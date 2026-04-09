package com.edbplus.db.jpa.model.array;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Table(name = "test_array_demo")
@Data
public class TestArrayDemo {
    @Id
    @Column(name="id")
    private Integer id;

    @Column(name="text_array")
//    private  String[] textArray;
    private  List<String> textArray;

}