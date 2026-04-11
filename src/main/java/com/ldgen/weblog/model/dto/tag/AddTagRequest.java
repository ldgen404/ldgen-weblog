package com.ldgen.weblog.model.dto.tag;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddTagRequest {

    @NotEmpty(message = "标签名称不能为空")
    private List<String> tagName;
}
