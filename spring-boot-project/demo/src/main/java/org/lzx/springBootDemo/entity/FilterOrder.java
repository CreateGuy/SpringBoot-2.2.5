package org.lzx.springBootDemo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author liuzhixuan
 * @date 2023-08-09
 */
@Data
@AllArgsConstructor
public class FilterOrder {

	private Integer order;

	private String filterName;
}
