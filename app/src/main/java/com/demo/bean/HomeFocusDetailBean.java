package com.demo.bean;

/**
 * Created by Administrator on 2023/8/6.
 */

public class HomeFocusDetailBean {
    private String pic_url;//图片链接
    private String link;//跳转链接
    private String link_type;//跳转类型：1H5,2单品页
    private String title;//标题
    private String pid;//商品id
    private String mini_path;//小程序路径

    public String getPic_url() {
        return pic_url;
    }

    public void setPic_url(String pic_url) {
        this.pic_url = pic_url;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink_type() {
        return link_type;
    }

    public void setLink_type(String link_type) {
        this.link_type = link_type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getMini_path() {
        return mini_path;
    }

    public void setMini_path(String mini_path) {
        this.mini_path = mini_path;
    }

}
