package org.pkislov.task2.dto;import jakarta.validation.constraints.NotNull;import lombok.Data;@Datapublic class SignalDto {    @NotNull(message = "id cannot be null")    private Long id;    @NotNull(message = "x cannot be null")    private double x;    @NotNull(message = "y cannot be null")    private double y;}