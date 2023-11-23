/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package jm.com.dpbennett.fm.manager;

import java.util.UUID;

/**
 *
 * @author Desmond Bennett
 */
public class Banner {

    private String id;
    private String itemImageSrc;
    private String thumbnailImageSrc;
    private String alt;
    private String title;
    private String url;

    public Banner() {
        this.id = UUID.randomUUID().toString().substring(0, 8);
    }

    public Banner(String itemImageSrc,
            String thumbnailImageSrc, 
            String alt, 
            String title,
            String url) {
        this();
        this.itemImageSrc = itemImageSrc;
        this.thumbnailImageSrc = thumbnailImageSrc;
        this.alt = alt;
        this.title = title;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getItemImageSrc() {
        return itemImageSrc;
    }

    public void setItemImageSrc(String itemImageSrc) {
        this.itemImageSrc = itemImageSrc;
    }

    public String getThumbnailImageSrc() {
        return thumbnailImageSrc;
    }

    public void setThumbnailImageSrc(String thumbnailImageSrc) {
        this.thumbnailImageSrc = thumbnailImageSrc;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
