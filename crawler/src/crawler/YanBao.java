package crawler;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.tuibei.crawler.beans.YanBaoBean;
import com.tuibei.crawler.po.YanBaoPo;

public class YanBao {
	static DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	static String longUrl="http://www.tuibei.com.cn/member.php?mod=logging&action=login&loginsubmit=yes&infloat=yes&lssubmit=yes&inajax=1";
	public static void main(String[] args) {
		List<YanBaoBean> beanList = new ArrayList<YanBaoBean>(50);
		String baseUrl = getBaseUrl(50,1);
		YanBaoPo yanBaoPo = getYanBo(baseUrl);
		for(int i=5;i>0;i--){
			baseUrl = getBaseUrl(50,i);
			yanBaoPo = getYanBo(baseUrl);
			if(yanBaoPo == null) break;
			beanList.addAll(convert2List(yanBaoPo.getData()));
			Collections.reverse(beanList);
			System.out.println(beanList.size());
			System.out.println("研报抓取完成……当前进度：第"+i+"页,共"+yanBaoPo.getPages()+"页");
			faTie(beanList);
			beanList.clear();
			try {
				Thread.sleep(new Random().nextInt(50000));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static String getNewerDate(CloseableHttpClient httpclient) throws Exception{
		HttpGet hashGet = getGetMethod("http://www.tuibei.com.cn/forum.php?mod=forumdisplay&fid=85");
		CloseableHttpResponse hashResponse = httpclient.execute(hashGet);
		HttpEntity hashentity = hashResponse.getEntity();
        if (hashentity != null) {              
            String buffer = EntityUtils.toString(hashentity, "UTF-8");
            Elements  tieZiList = Jsoup.parse(buffer).getElementsByAttributeValue("class","comeing_bd");
            for(Element tr: tieZiList ){
            	Elements  ths = tr.getElementsByTag("th");
            	for(Element th : ths){
            		 Elements as = th.getElementsByTag("a");
            		 if(as.size()>=2){
            			 Element a = as.get(1);
            			 if(a!=null && a.hasText()){
            				 String title = as.get(1).html();
                			 String date = title.substring(title.indexOf("【")+1,title.indexOf("【")+1+10);
                			 return date;
            			 }
            		 }
            	}
            }
        }  
        hashGet.releaseConnection();
        return "";
	}
	
	
	public static CloseableHttpClient login(){
		CloseableHttpClient loginClient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(longUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("fastloginfield", "username"));
        params.add(new BasicNameValuePair("username", "javalive"));
        params.add(new BasicNameValuePair("password", "411327"));
        params.add(new BasicNameValuePair("quickforward", "yes"));
        params.add(new BasicNameValuePair("handlekey", "ls"));
        httppost.setEntity(new UrlEncodedFormEntity(params,Consts.UTF_8));
        CloseableHttpResponse response;
		try {
			response = loginClient.execute(httppost);
			if(response.getStatusLine().getStatusCode()==200) {
	        	System.out.println("login success!!");
	        	getNewerDate(loginClient);
	        	return loginClient;
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			httppost.releaseConnection();
		}
		return null;
	}
	
	public static Map<String,String> constParaMap(){
		HashMap<String, String> nv = new HashMap<String, String>();
		nv.put("posttime", (System.currentTimeMillis()/1000)+"");
		nv.put("allownoticeauthor", "1");
		nv.put("ordertype", "1");
		nv.put("replycredit_extcredits", "0");
		nv.put("replycredit_membertimes", "1");
		nv.put("replycredit_random", "100");
		nv.put("replycredit_times", "1");
		nv.put("save", "");
		nv.put("usesig", "1");
		nv.put("wysiwyg", "1");
		return nv;
	}
	
	public static void faTie(List<YanBaoBean> beanList){
		 try {
			 CloseableHttpClient httpclient = login();
			 String lastDt = getNewerDate(httpclient);
			 Date lastDate = sdf.parse(lastDt);
			 int n = 0;
			if(httpclient != null && beanList.size()>0) {
				for(YanBaoBean bean : beanList){
					if(sdf.parse(bean.getUptime()).getTime()<=lastDate.getTime()) continue;
					n++;
					HashMap<String, String> nv = new HashMap<String, String>();
					nv.put("subject", bean.getYanbaoTitle());
					nv.put("message",bean.getYanbaoContent());
					nv.put("tags",bean.getTages());
					nv.put("formhash",getHash(httpclient));
					nv.putAll(constParaMap());
					HttpPost tieziPost = getPostMethod("http://www.tuibei.com.cn/forum.php?mod=post&action=newthread&fid=85&extra=&topicsubmit=yes",nv);
					tieziPost.addHeader("Referer","http://www.tuibei.com.cn/forum.php?mod=post&action=newthread&fid=85");
					int status = -10;
					while(!(status == 200 || status == 301)){
						try {
							 CloseableHttpResponse tieziResponse = httpclient.execute(tieziPost);
							 HttpEntity entity = tieziResponse.getEntity();
							    if (entity != null) {              
							    	 String buffer = EntityUtils.toString(entity, "UTF-8"); 
					                 System.out.println("页面："+buffer.toString());
					             } 
							status = tieziResponse.getStatusLine().getStatusCode();
							tieziPost.releaseConnection();
							System.out.println("发帖成功！！"); 
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if(n%5==0){
						httpclient.close();
						Thread.sleep(2000);
						httpclient = login();
					}
					System.out.println("帖子进度："+n);
				}
				httpclient.close();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	public static String getHash(CloseableHttpClient httpclient) throws Exception{
		HttpGet hashGet = getGetMethod("http://www.tuibei.com.cn/forum.php?mod=post&action=newthread&fid=85");
		CloseableHttpResponse hashResponse = httpclient.execute(hashGet);
		HttpEntity hashentity = hashResponse.getEntity();
		String formhash ="";
        if (hashentity != null) {              
            String buffer = EntityUtils.toString(hashentity, "UTF-8");   
            int s = buffer.indexOf("id=\"formhash\" value=\"");
            s += 21;
            formhash = buffer.substring(s, s + 8);
            System.out.println("formhash:" + formhash);
            //end 读取整个页面内容  
        }  
        hashGet.releaseConnection();
        return formhash;
	}
	
	
	 public final static HttpGet getGetMethod(String url){
	        HttpGet httpget = new HttpGet(url);
	        httpget.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;");
	        httpget.addHeader("Accept-Language", "zh-cn");
	        httpget.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
	        httpget.addHeader("Accept-Charset", "utf-8");
	        httpget.addHeader("Keep-Alive", "30000");
	        httpget.addHeader("Connection", "Keep-Alive");
	        httpget.addHeader("Cache-Control", "no-cache");
	        return httpget;
	 }
	
	 
	 /**
	     * 获取Post方式HttpMethod
	     * 
	     * @param url
	     *            请求的URL
	     * @return
	     */
	    public final static HttpPost getPostMethod(String URL, HashMap<String, String> nameValuePair){
	    	HttpPost post = new HttpPost(URL);
	        post.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;");
	        post.addHeader("Accept-Language", "zh-cn");
	        post.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
	        post.addHeader("Accept-Charset", "utf-8");
	        post.addHeader("Keep-Alive", "300");
	        post.addHeader("Connection", "Keep-Alive");
	        post.addHeader("Cache-Control", "no-cache");
	 
	        int size = nameValuePair.values().size();
	        if (size == 0)  return post;
	        NameValuePair[] param = new NameValuePair[size];
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        Iterator iter = nameValuePair.entrySet().iterator();
	        while (iter.hasNext()){
	            Map.Entry entry = (Map.Entry) iter.next();
	            params.add(new BasicNameValuePair(String.valueOf(entry.getKey()),String.valueOf(entry.getValue())));
	        }
	        post.setEntity(new UrlEncodedFormEntity(params,Consts.UTF_8));
	        return post;
	    }
	 
	public static YanBaoPo getYanBo(String baseUrl){
		String resStr = httpRequest(baseUrl);
		if("".equals(resStr)) return null; 
		String jsonStr = resStr.split("=")[1];
		jsonStr = jsonStr.substring(0,jsonStr.length()-1);
		try {
			return new ObjectMapper().readValue(jsonStr, YanBaoPo.class);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	
	public static String getBaseUrl(int pageSize,int page){
		String url="http://data.eastmoney.com/reportold/data.aspx?style=ggyb&tp=&cg=&dt=m6&jg=&pageSize=%s&page=%s&jsname=tQZgsukS&rt="+System.currentTimeMillis()/1000;
		System.out.println("源url:"+String.format(url, pageSize,page));
		return String.format(url, pageSize,page);
	}
	
	public static String httpRequest(String curl){
		 CloseableHttpClient httpclient = HttpClients.createDefault();
		 //httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,5000);
		 HttpGet httpget = new HttpGet(curl);
		 try {
			CloseableHttpResponse response = httpclient.execute(httpget);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				int status = response.getStatusLine().getStatusCode();
                return EntityUtils.toString(entity, "UTF-8"); 
            }
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return "";
	}
	
	
	public static List<YanBaoBean> convert2List(List<String> strList){
		List<YanBaoBean> list = new ArrayList<YanBaoBean>();
		if(strList!=null && strList.size()>=15){
			for(String line : strList){
				String code = line.split(",")[0];
				String name = line.split(",")[1];
				String uptime = line.split(",")[4];
				if(uptime.split("-").length<=2) continue;
				String recommand = line.split(",")[5];
				String recommandCompay = line.split(",")[7];
				String yanbaoUrl = line.split(",")[13];
				String yanbaoTitle = line.split(",")[14];
				String yanbaoContent = "http://data.eastmoney.com/report/"+uptime.replace("-","")+"/"+yanbaoUrl+".html";
				yanbaoContent = getYanbaoComment(yanbaoContent);
				list.add(new YanBaoBean(code,name,uptime,recommand,recommandCompay,yanbaoUrl,yanbaoTitle,yanbaoContent));
			}
		}
		return list;
	} 
	
	
    /**
     * 获取博客上的文章标题和链接
     */
    public static String getYanbaoComment(String commentUrl) {
        try {
			Document yanbaoDoc = Jsoup.connect(commentUrl).timeout(5000).get();
			Element yanbaoElement = yanbaoDoc.getElementById("ContentBody");
			String yanbaoContent = yanbaoElement.html().split("<!-- EM_StockImg_End -->")[1];
			yanbaoContent = yanbaoContent.replace("<p>", "").replace("</p>", "\r\n");
			System.out.println("研报内容："+yanbaoContent);
            return yanbaoContent; 
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
}
