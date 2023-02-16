package org.lzx.springBootDemo.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 对账单列表
 * </p>
 *
 * @author zengfeiyue
 * @since 2022-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Statements implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 对账单号
     */
    private Long id;

    /**
     * 生成人id
     */
    private Integer userId;

    /**
     * 生成名称
     */
    private String username;

    /**
     * 订单所属公司代号-对应s4_company表中的code字段
     */
    private String companyCode;

    /**
     * 交付公司
     */
    private String deliverCompanyCode;

    /**
     * 交付公司名称
     */
    private String deliverCompanyName;

    /**
     * 客户编码
     */
    private String customerCode;

    /**
     * 销售发票金额
     */
    private BigDecimal amount;

    /**
     * 欠款金额(需要核销金额)
     */
    private BigDecimal arrearsAmount;

    /**
     * 销售发票号，多个逗号隔开
     */
    private String orderSn;

    /**
     * epk任务id 对应ekp_task表中的id
     */
    private Integer taskId;

    /**
     * 交货单号，多个逗号隔开
     */
    private String deliverySn;

    /**
     * wms单号，多个逗号隔开
     */
    private String wmsNum;

    /**
     * 对账单备注
     */
    private String remark;

    /**
     * 销售发票单留言
     */
    private String orderRemark;

    /**
     * 0-待审核，1-已通过，2-已退回,3-不显示（S4调整凭证过来的手工单） 4：撤回
     */
    private Integer status;

    /**
     * 操作人id
     */
    private Integer operatorId;

    /**
     * 操作人名字
     */
    private String operatorName;

    /**
     * 操作人备注
     */
    private String operatorRemark;

    /**
     * 回退时间
     */
    private LocalDateTime rollbackDate;

    /**
     * 终端客户名称
     */
    private String terminal;

    /**
     * 开票状态-1线下开票0-未开票-1-部分开票-2-已开票
     */
    private Integer iStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * 金税发票号，多个逗号隔开
     */
    private String taxNumList;

    /**
     * 通过时间
     */
    private LocalDateTime passDate;

    /**
     * 对应商品欠款金额
     */
    private BigDecimal goodsAmount;

    /**
     * 回退备注
     */
    private String rollbackRemark;

    /**
     * 是否自动生成对账单
     */
    private Integer isAutomatic;

    /**
     * 回退操作人id
     */
    private Integer rollbackOperatorId;

    /**
     * 回退操作人名字
     */
    private String rollbackOperatorName;

    /**
     * 用于逐单生成开票申请备注
     */
    private String invoiceRemark;

    /**
     * 0-未签收,1-已签收
     */
    private Integer signIn;

    /**
     * 0-否,1-是完全第三方销售单生成对账单
     */
    private Integer isOtherOrder;

    /**
     * 对账Excel模板地址
     */
    private String excel;

    /**
     * 核销状态0：未核销，部分核销，1：全部核销
     */
    private Integer offStatus;

    /**
     * 数据来源
     * {@link cn.ofs.t3.common.enums.DataSourcesEnum}
     */
    private Integer dataSources;
}
