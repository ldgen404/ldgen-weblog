package com.ldgen.weblog.model.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFileResqest {

    /**
     * 文件的访问链接
     */
    private String url;

}
