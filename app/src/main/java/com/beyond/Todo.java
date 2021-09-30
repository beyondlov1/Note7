package com.beyond;

import com.time.TimeNLPUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.Date;

public class Todo {
    private String originText;
    private Long remindTime;
    private Boolean reminded;
    private Source source = Source.CREATE;

    public static Todo origin(String line) {
        Todo todo = new Todo();
        todo.setOriginText(line);
        todo.setSource(Source.READ);
        return todo;
    }

    public String toFormattedLine() {
        StringBuilder sb = new StringBuilder();
        if (originText != null) {
            sb.append(originText);
            if (remindTime != null) {
                sb.append("|");
                sb.append(DateFormatUtils.format(remindTime, "yyyy-MM-dd HH:mm"));
                sb.append("|");
                if (reminded != null) {
                    sb.append(reminded);
                }
            }
        }
        return sb.toString();
    }


    public static Todo parseFrom(String line) throws ParseException {

        if (StringUtils.isBlank(line)){
            return Todo.origin(line);
        }

        String[] split = StringUtils.split(line, "|");
        if (split.length >= 3){
            Todo todo = new Todo();
            todo.setOriginText(split[0]);
            Date parsedDate = DateUtils.parseDate(StringUtils.trim(split[1]), "yyyy-MM-dd HH:mm");
            todo.setRemindTime(parsedDate.getTime());
            todo.setReminded(Boolean.parseBoolean(StringUtils.trim(split[2])));
            todo.setSource(Source.READ);
            return todo;
        }

        if (split.length == 2){
            Todo todo = new Todo();
            todo.setOriginText(split[0]);
            Date parsedDate = DateUtils.parseDate(StringUtils.trim(split[1]), "yyyy-MM-dd HH:mm");
            todo.setRemindTime(parsedDate.getTime());
            todo.setReminded(false);
            todo.setSource(Source.READ);
            return todo;
        }

        Date parsedDate = TimeNLPUtil.parse(line);
        if (parsedDate == null){
            return Todo.origin(line);
        }
        Todo todo = new Todo();
        todo.setOriginText(line);
        todo.setRemindTime(parsedDate.getTime());
        todo.setReminded(false);
        todo.setSource(Source.PARSE);
        return todo;
    }

    public String getOriginText() {
        return originText;
    }

    public void setOriginText(String originText) {
        this.originText = originText;
    }

    public Long getRemindTime() {
        return remindTime;
    }

    public void setRemindTime(Long remindTime) {
        this.remindTime = remindTime;
    }

    public Boolean getReminded() {
        return reminded;
    }

    public void setReminded(Boolean reminded) {
        this.reminded = reminded;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }


    public enum Source {
        READ, PARSE, CREATE
    }

}
