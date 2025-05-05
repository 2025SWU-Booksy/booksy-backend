package com.booksy.domain.category.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryCsvRow {

  @CsvBindByName(column = "cid")
  private String cid;

  @CsvBindByName(column = "name")
  private String name;

  @CsvBindByName(column = "mall")
  private String mall;

  @CsvBindByName(column = "depth1")
  private String depth1;

  @CsvBindByName(column = "depth2")
  private String depth2;

  @CsvBindByName(column = "depth3")
  private String depth3;

  @CsvBindByName(column = "depth4")
  private String depth4;

  @CsvBindByName(column = "depth5")
  private String depth5;

  @CsvBindByName(column = "depth6")
  private String depth6;

  @CsvBindByName(column = "depth7")
  private String depth7;
}
