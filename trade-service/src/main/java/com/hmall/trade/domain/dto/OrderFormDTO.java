package com.hmall.trade.domain.dto;

import com.hmall.api.domain.dto.OrderDetailDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import lombok.Data;

@Data
@ApiModel(description = "交易下单表单实体")
public class OrderFormDTO {
    @ApiModelProperty("收货地址id")
    private Long addressId;
    @ApiModelProperty("支付类型")
    private Integer paymentType;
    @ApiModelProperty("下单商品列表")
    private List<OrderDetailDTO> details;
}
