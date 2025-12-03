package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class SpaController {

    // URLに「.」が含まれていない場合（＝APIや静的ファイル以外）は、index.htmlへ転送
    // これにより、/login や /training などのReactルートが正しく動作し、
    // かつ main.css や logo.png などのファイルは正常に読み込まれます。
    @RequestMapping(value = "/**/{path:[^\\.]*}")
    public String redirect() {
        return "forward:/index.html";
    }
}