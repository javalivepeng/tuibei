package com.tuibei.crawler.beans;

public class YanBaoBean {
	private String code;
	private String name;
	private String uptime;
	private String recommand;
	private String recommandCompany;
	private String yanbaoUrl;
	private String yanbaoTitle;
	private String yanbaoContent;
	public String getYanbaoContent() {
		return yanbaoContent;
	}

	public void setYanbaoContent(String yanbaoContent) {
		this.yanbaoContent = yanbaoContent;
	}

	public YanBaoBean() {
	}
	
	public YanBaoBean(String code, String name, String uptime,
			String recommand, String recommandCompany, String yanbaoUrl,
			String yanbaoTitle,String yanbaoContent) {
		super();
		this.code = code;
		this.name = name;
		this.uptime = uptime;
		this.recommand = recommand;
		this.recommandCompany = recommandCompany;
		this.yanbaoUrl = yanbaoUrl;
		this.yanbaoTitle = yanbaoTitle;
		this.yanbaoContent = yanbaoContent;
	}



	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUptime() {
		return uptime;
	}

	public void setUptime(String uptime) {
		this.uptime = uptime;
	}

	public String getRecommand() {
		return recommand;
	}

	public void setRecommand(String recommand) {
		this.recommand = recommand;
	}

	public String getRecommandCompany() {
		return recommandCompany;
	}

	public void setRecommandCompany(String recommandCompany) {
		this.recommandCompany = recommandCompany;
	}

	public String getYanbaoUrl() {
		return yanbaoUrl;
	}

	public void setYanbaoUrl(String yanbaoUrl) {
		this.yanbaoUrl = yanbaoUrl;
	}

	public String getYanbaoTitle() {
		yanbaoTitle = yanbaoTitle.replace("&sbquo;", ",");
		return "【"+uptime+"】"+name+":"+yanbaoTitle.substring(0,yanbaoTitle.length()>=50?50:yanbaoTitle.length());
	}

	public void setYanbaoTitle(String yanbaoTitle) {
		this.yanbaoTitle = yanbaoTitle;
	}
	
	public String getTages(){
		return code+","+name+","+recommand+","+recommandCompany;
	}
	
	public static void main(String[] args) {
		System.out.println(503%5);
	}

	@Override
	public String toString() {
		return "YanBaoBean [code=" + code + ", name=" + name + ", uptime="
				+ uptime + ", recommand=" + recommand + ", recommandCompany="
				+ recommandCompany + ", yanbaoUrl=" + yanbaoUrl
				+ ", yanbaoTitle=" + yanbaoTitle + ", yanbaoContent="
				+ yanbaoContent + "]";
	}
	

	
}
