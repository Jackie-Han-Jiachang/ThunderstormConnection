package bnds.chinese.dto;

import bnds.chinese.model.CharacterId;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.LinkedHashSet;
import java.util.Set;

public record EventRequest(
        @NotBlank(message = "请输入事件名称")
        @Size(max = 30, message = "事件名称不能超过30个字符")
        String name,
        @NotBlank(message = "请输入事件描述")
        @Size(max = 50, message = "事件描述不能超过50个字符")
        String description,
        @NotNull(message = "请选择发起人")
        CharacterId initiator,
        @NotEmpty(message = "请至少选择一位被影响人")
        Set<CharacterId> affectedCharacters,
        @Min(value = -10, message = "好感度变化不能小于-10")
        @Max(value = 10, message = "好感度变化不能大于10")
        int affectionDelta
) {
    public EventRequest {
        affectedCharacters = affectedCharacters == null ? new LinkedHashSet<>() : new LinkedHashSet<>(affectedCharacters);
    }
}
