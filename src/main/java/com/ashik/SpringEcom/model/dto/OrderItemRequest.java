package com.ashik.SpringEcom.model.dto;

public record OrderItemRequest(
        int productId,
        int quantity
) {

}
