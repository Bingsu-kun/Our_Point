package com.webproject.ourpoint.model.marker;

public enum Category {

  FISHING("CATEGORY_FISHING"),CAMPING("CATEGORY_CAMPING"),GATHERING("CATEGORY_GATHERING"),STORE("CATEGORY_STORE"),SECRET_PLACE("CATEGORY_SECRET");

  private String value;

  Category(String value) {this.value = value;}

  public String category() { return value; }

  public static Category of(String name) {
    for (Category category : Category.values()) {
      if (category.name().equalsIgnoreCase(name)) {
        return category;
      }
    }
    return null;
  }

}
