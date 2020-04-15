package com.mubaracktahir.blog.model;

/**
 * Created by Mubarak Tahir on 3/26/2020.
 * Mubby inc
 * mubarack.tahirr@gmail.com
 */
public class Blog {
    private String description;
    private String title;
    private String image;

    public Blog(String description, String title, String image, String profileimage, String name) {
        this.description = description;
        this.title = title;
        this.image = image;
        this.profileimage = profileimage;
        this.name = name;
    }

    public String getProfileimage() {
        return profileimage;
    }

    public void setProfileimage(String profileimage) {
        this.profileimage = profileimage;
    }

    private String profileimage;

    public Blog(String description, String title, String image, String name) {
        this.description = description;
        this.title = title;
        this.image = image;
        this.name = name;
    }

    private String name;


    public Blog(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
