package com.myself.sEverything.cmd;

import com.myself.sEverything.config.sEverythingConfig;
import com.myself.sEverything.core.common.Message;
import com.myself.sEverything.core.model.Condition;
import com.myself.sEverything.core.model.FileType;
import com.myself.sEverything.core.model.Thing;
import com.myself.sEverything.core.sEverythingManager;

import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class sEverythingApp {
    private static Scanner scanner = new Scanner(System.in);
    private static LinkedList<String> historicalRecordsList = new LinkedList<>();
    private static int maxNum = 5;

    public static void main(String[] args) {
        param(args);
        welcome();
        sEverythingManager manager = sEverythingManager.getInstance();
        manager.startThingClearThread();
        manager.startFileSystemMonitor();
        interactive(manager);
    }

    private static void printHR(LinkedList<String> historicalRecordsList) {
        if (historicalRecordsList.size() != 0) {
            System.out.println("历史搜索记录：");
            for (String l : historicalRecordsList) {
                System.out.print(l + " ");
            }
            System.out.println();
        }
    }

    private static void param(String[] args) {
        sEverythingConfig config = sEverythingConfig.getInstance();
        for (String param : args) {
            String maxReturnStr = "--maxReturnThingRecode=";
            if (param.startsWith(maxReturnStr)) {
                int index = param.indexOf("=");
                if (param.length() > index + 1) {
                    String maxReturnParam = param.substring(index + 1);
                    //如果输入的为负数 或 不为整数 -> 数据格式异常
                    config.setMaxReturnThingRecode(Integer.parseInt(maxReturnParam));
                }
            }

            String orderByAscStr = "--orderByAsc=";
            if (param.startsWith(orderByAscStr)) {
                int index = param.indexOf("=");
                if (param.length() > index + 1) {
                    String orderByAscParam = param.substring(index + 1);
                    //true -> true
                    //其他 -> false
                    //所以只有输入true时才为true，其余不管输入什么，都是false
                    config.setOrderByAsc(Boolean.parseBoolean(orderByAscParam));
                }
            }

            String intervalStr = "--interval=";
            if (param.startsWith(intervalStr)) {
                int index = param.indexOf("=");
                if (param.length() > index + 1) {
                    //输入负数 -> IllegalArgumentException: timeout value is negative
                    //输入非整数 -> NumberFormatException
                    String intervalParam = param.substring(index + 1);
                    config.setInterval(Long.parseLong(intervalParam));
                }
            }

            String includePathStr = "--includePath=";
            if (param.startsWith(includePathStr)) {
                int index = param.indexOf("=");
                if (param.length() > index + 1) {
                    //输入时以逗号隔开
                    String[] includePaths = param.substring(index + 1).split(";");
                    config.getIncludePath().clear();
                    for (String includePath : includePaths) {
                        config.getIncludePath().add(includePath);
                    }
                }
            }

            String excludePathStr = "--excludePath=";
            if (param.startsWith(excludePathStr)) {
                int index = param.indexOf("=");
                if (param.length() > index + 1) {
                    String[] excludePaths = param.substring(index + 1).split(";");
                    config.getExcludePath().clear();
                    for (String excludePath : excludePaths) {
                        config.getExcludePath().add(excludePath);
                    }
                }
            }
        }
        System.out.println(config);
    }

    private static void interactive(sEverythingManager manager) {
        while (true) {
            System.out.print(">>");
            String line = scanner.nextLine();
            if (line.startsWith("search")) {
                String[] values = line.split(" ");
                if (!values[0].equals("search")) {
                    help();
                    continue;
                }
                if (values.length >= 2) {
                    Condition condition = new Condition();
                    condition.setName(values[1]);
                    StringBuilder sb = new StringBuilder(values[1]);
                    if (values.length >= 3) {
                        boolean flag = false;
                        for (FileType fileType : FileType.values()) {
                            if (fileType.name().equalsIgnoreCase(values[2])) {
                                flag = true;
                                condition.setFileType(values[2].toUpperCase());
                                sb.append("(").append(values[2].toUpperCase()).append(")");
                            }
                        }
                        if (!flag) {
                            help();
                            continue;
                        }
                    }
                    String record = sb.toString();
                    if (historicalRecordsList.contains(record)) {
                        historicalRecordsList.remove(record);
                    }
                    if (historicalRecordsList.size() == maxNum) {//没到达存储历史记录最大个数的情况下
                        historicalRecordsList.removeLast();
                    }
                    historicalRecordsList.addFirst(record);
                    search(manager, condition);
                    printHR(historicalRecordsList);
                    continue;
                } else {
                    help();
                    continue;
                }
            }
            switch (line) {
                case "help":
                    help();
                    break;
                case "quit":
                    quit();
                    break;
                case "index":
                    index(manager);
                    break;
                default:
                    help();
            }
        }
    }

    public static void search(sEverythingManager manager, Condition condition) {
        condition.setLimit(sEverythingConfig.getInstance().getMaxReturnThingRecode());
        condition.setOrderByAsc(sEverythingConfig.getInstance().getOrderByAsc());
        System.out.println("Start searching...");
        long start = System.currentTimeMillis();
        List<Thing> list = manager.search(condition);
        for (Thing thing : list) {
            System.out.println(Message.print(thing));
        }
        long end = System.currentTimeMillis();
        System.out.println("Finish searching...");
        System.out.println("Cost time of searching is " + (end - start) + " milli");

    }

    private static void index(sEverythingManager manager) {
        new Thread(manager::buildIndex).start();
    }

    private static void quit() {
        System.out.println("谢谢使用，再见");
        System.exit(0);
    }

    private static void help() {
        System.out.println("命令行如下：");
        System.out.println("帮助：help");
        System.out.println("退出：quit");
        System.out.println("索引：index");
        System.out.println("检索：search <name> [<fileType> img | doc | bin | archive | other]");
    }

    private static void welcome() {
        System.out.println("欢迎使用Everything");
    }
}
