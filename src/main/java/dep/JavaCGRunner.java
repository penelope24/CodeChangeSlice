//package fy.ACE;
//
//import java.io.*;
//import java.util.ArrayList;
//import java.util.List;
//
//public class JavaCGRunner {
//
//    public static void execute_single(String jarPath, String cwd, String outputPath) {
//        List<String> args = new ArrayList<>();
//        args.add("java");
//        args.add("-jar");
//        args.add("javacg-0.1-SNAPSHOT-static.jar");
//        args.add(jarPath);
//        ProcessBuilder pb = new ProcessBuilder();
//        pb.command(args);
//        pb.directory(new File(cwd));
//        try {
//            Process process = pb.start();
//            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        pb.redirectOutput(new File(outputPath + "/test.txt"));
//    }
//
//    public static void execute_batch(List<String> jarFiles, String cwd, String outputPath) {
//        List<String> args = new ArrayList<>();
//        args.add("java");
//        args.add("-jar");
//        args.add("javacg-0.1-SNAPSHOT-static.jar");
//        args.addAll(jarFiles);
//        ProcessBuilder pb = new ProcessBuilder();
//        pb.command(args);
//        pb.directory(new File(cwd));
//        try {
//            Process process = pb.start();
//            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            String line;
//            while ((line = br.readLine()) != null) {
//                System.out.println(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
////        pb.redirectOutput(new File(outputPath + "/test.txt"));
//    }
//}
