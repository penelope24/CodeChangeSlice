//package fy;
//
//public class CLI {
//    Execution exec;
//
//    public CLI() {
//        this.exec = new Execution();
//    }
//
//    public Execution parse(String[] args) {
//        for (int i=0; i<args.length; i++) {
//            if (args[i].startsWith("-")) {
//                String opt = args[i].substring(1).toLowerCase();
//                switch (opt) {
//                    case "type": {
//                        if (i < args.length - 1) {
//                            i++;
//                            switch (args[i]) {
//                                case "all":
//                                    exec.setRunType(Execution.RunType.run_all_projects);
//                                    break;
//                                case "single":
//                                    exec.setRunType(Execution.RunType.run_single_project);
//                                    break;
//                                case "continue":
//                                    exec.setRunType(Execution.RunType.run_continue);
//                                    break;
//                                case "reproduce":
//                                    exec.setRunType(Execution.RunType.run_reproduce);
//                                    break;
//                            }
//                        }
//                        else {
//                            System.out.println("param not specified");
//                        }
//                        break;
//                    }
//                    case "base": {
//                        if (i < args.length - 1) {
//                            i++;
//                            exec.setAll_projects_base(args[i]);
//                        }
//                        break;
//                    }
//                    case "project": {
//                        if (i < args.length - 1) {
//                            i++;
//                            exec.setCurr_project_path(args[i]);
//                        }
//                        break;
//                    }
//                    case "version": {
//                        if (i < args.length - 1) {
//                            i++;
//                            exec.setCurr_version(args[i]);
//                        }
//                        break;
//                    }
//                    case "output": {
//                        if (i < args.length - 1) {
//                            i++;
//                            exec.setOutput_path(args[i]);
//                        }
//                        else {
//                            System.out.println("param not specified");
//                        }
//                        break;
//                    }
//                }
//            }
//        }
//        return exec;
//    }
//}
