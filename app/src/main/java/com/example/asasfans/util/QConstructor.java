package com.example.asasfans.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author akarinini
 * @author LEN5010
 * @description 视频搜索 q 参数构造工具，拼接字段、关键字和查询类型。
 */
public class QConstructor {
    private List<QArray> qArrays;

    public QConstructor(List<QArray> qArrays) {
        this.qArrays = qArrays;
    }
    public String toString(){
        StringBuilder tmp = new StringBuilder();
        for (int i = 0; i < qArrays.size(); i++){
            // 多组查询条件用 ~ 分隔，接口侧会按字段和逻辑类型解析。
            tmp.append(qArrays.toString());
            if (i != qArrays.size() - 1){
                tmp.append("~");
            }
        }
        return tmp.toString();
    }


    public static class QArray {
        private String key;
        private List<String> keywords;
        private String type;
        public QArray(String key, List<String> keywords, String type) {
            this.key = key;
            this.keywords = keywords;
            this.type = type;
        }

        public String toString(){
            StringBuilder tmp = new StringBuilder();
            tmp.append(key);
            tmp.append(".");

            if (keywords.size() == 0){
                tmp.append(".");
            }
            // pubdate 查询允许两个空 keyword，必须保留一个空字段占位。
            //为了兼容pubdate
            if (keywords.size() == 2 && keywords.get(0).equals("") && keywords.get(1).equals("")){
                tmp.append(".");
            }else {
                for (int i = 0; i < keywords.size(); i++) {
                    tmp.append(keywords.get(i));
                    if (i == keywords.size() - 1) {
                        tmp.append(".");
                    } else {
                        tmp.append("+");
                    }
                }
            }
            tmp.append(type);
            return tmp.toString();
        }

        public String getType() {
            return type;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public void setKeywords(List<String> keywords) {
            this.keywords = keywords;
        }

        public void setType(String type) {
            this.type = type;
        }
    }


    public void main(String[] args) {
        List<QArray> arrays = new ArrayList<>();
        arrays.add(new QArray("tag", Arrays.asList("Buenos Aires", "Córdoba", "La Plata"), "OR"));
        arrays.add(new QArray("tag", Arrays.asList("Buenos Aires", "Córdoba", "La Plata"), "OR"));
        System.out.println(new QConstructor(arrays));
    }

}
