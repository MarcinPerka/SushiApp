package com.archu.sushiapp.db.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SushiCategory {

    private String category;
    private String description;
    private String image;
    private String basicInfo;
    private String imageVocabulary;

}
