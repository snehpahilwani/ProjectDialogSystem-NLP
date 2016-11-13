package com.dialogGator;

import static java.lang.Integer.parseInt;

public class Product {

    public int id;
    public String title;
    public String gender;
    public String category;
    public double price;
    public String color;
    public String size;
    public String brand;
    public String imgUrl;

    public Product(){}
    public Product(String itemId, String name, String description, double price)
    {
        this.id = parseInt(itemId);
        this.title = name;
        this.price = price;
    }

    public String getName()
    {
        return title;
    }

    public String getProductId()
    {
        return Integer.toString(id);
    }

    public String getDescription()
    {
        return "No Description";
    }

    public double getPrice()
    {
        return price;
    }
}
