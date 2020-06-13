/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package syntaxit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Enache
 */
public class Parser {

    private final File devdesc;
    private File syntax_template;
    private File vlan_syntax_template;
    private FileInputStream fr;
    private BufferedReader br;
    private File vlan_acc_syntax_template;
    private File vlan_man_syntax_template;

    public Parser(File devdesc) {
        this.devdesc = devdesc;
    }

    public ArrayList<Device> scanForDevs() throws FileNotFoundException, IOException {
        ArrayList<Device> discovered_devices = new ArrayList<>();
        fr = new FileInputStream(devdesc);
        br = new BufferedReader(new InputStreamReader(fr));
        ArrayList<String> keys = new ArrayList<String>();
        String raw_content = "";

        HashMap<String, String> devs = new HashMap<>();
        String line = "";
        while ((line = br.readLine()) != null) {

            if (line.indexOf("@") > 0) {
                String[] props = line.replaceAll("<", "").replaceAll(">", "").split("@");
                devs.put(props[1].trim(), props[0].trim());
                keys.add(props[1].trim());
               // System.out.println(props[1].trim() + " <> " + props[0].trim());

            } else {

            }
        }

      //  keys.forEach(System.out::println);
        int index = 0;

        fr.getChannel().position(0);
        br = new BufferedReader(new InputStreamReader(fr));
        while (br.ready()) {
            raw_content += br.readLine() + "\n";

        }

        ArrayList<String> raw_devs = new ArrayList<>();
        raw_devs.addAll(Arrays.asList(raw_content.split("<[A-Z]*@[A-Za-z1-9]*>")));
        raw_devs.removeIf(st -> {
            return st.length() < 5;
        });
        String[] raw_devices = Arrays.copyOf(raw_devs.toArray(), raw_devs.size(), String[].class);
        System.out.println(raw_devices.length);
        for (String raw_dev : raw_devices) {
            // System.out.println(raw_dev);
            TYPES.DEVICES dev_type = TYPES.DEVICES.valueOf(devs.get(keys.get(index)));
            Device dev = new Device(keys.get(index), dev_type);
            String[] raw_env_vars = raw_dev.split("\n");
            for (String rev : raw_env_vars) {
                TYPES.VARS detected_type = rev.indexOf("->") > 0 ? TYPES.VARS.KEY_VALUE : TYPES.VARS.SIMPLE;
                Variable var = null;
                if (detected_type.equals(TYPES.VARS.KEY_VALUE)) {
                    String[] first_level_props = rev.split("->");
                    String name = first_level_props[0].trim();
                    HashMap<String, String> kv = new HashMap<>();
                    var = new Variable<HashMap<String, String>>(detected_type, name, kv);
                } else if (rev.indexOf("=") > 0) {
                    String[] props = rev.split("=");

                    var = new Variable<String>(detected_type, props[0].trim(), props[1].trim());
                }
                if (var == null) {
                    continue;
                }

                dev.addEnvVariable(var);
            }
            discovered_devices.add(dev);
            index++;
        }

        return discovered_devices;
    }

    ArrayList<String> loadConf(Device dev) throws FileNotFoundException, IOException {
        System.out.println();
        ArrayList<String> lines = new ArrayList<>();
        this.syntax_template = new File(dev.type.toString().toLowerCase() + ".syntax");
        fr = new FileInputStream(syntax_template);
        br = new BufferedReader(new InputStreamReader(fr));
        String line = "";
        while ((line = br.readLine()) != null) {
            if (Pattern.compile("<<.*?>>").matcher(line).find()) {
                Pattern pattern = Pattern.compile("<<.*?>>");
                Matcher matcher = pattern.matcher(line);
                matcher.find();

                String to_bind_raw = matcher.group(0);
                String to_bind = to_bind_raw.replaceAll("<", "").replaceAll(">", "");
                Variable var = dev.getEnvVariable(to_bind);
                if (var != null) {
                    if (var.type.equals(TYPES.VARS.SIMPLE)) {
                        line = line.replace(to_bind_raw, (String) var.getValue());
                        System.out.println("line binded >> " + line);
                        lines.add(line);
                    } else {
                        System.out.println("***WARNING***\n Complex variable detected, skipping for now");
                    }
                } else {
                    System.out.println("***WARNING***\n Nonexistent env variable queried (" + to_bind + ")\n*************");
                }
            } else {
                lines.add(line);
            }

        }

        if (dev.type.equals(TYPES.DEVICES.SWITCH)) {
            loadVlanConfig(lines, dev);
        } else if (dev.type.equals(TYPES.DEVICES.ROUTER)) {
            loadInterfaceConfig(lines, dev);
            loadRoutingConfig(lines, dev);
        }

        return lines;
    }

    private void loadVlanConfig(ArrayList<String> current_conf, Device dev) {
        String role = (String) dev.getEnvVariable("role").value;
        String vlan_acc = (String) dev.getEnvVariable("vlan_acc").value;
        String vlan_acc_name = (String) dev.getEnvVariable("vlan_acc_name").value;
        String vlan_trunk = (String) dev.getEnvVariable("vlan_trunk").value;
        String vlan_null = (String) dev.getEnvVariable("vlan_null").value;

        ArrayList<String> vlan_syntax_template = new ArrayList<>();
        ArrayList<String> vlan_man_syntax_template = new ArrayList<>();
        this.vlan_acc_syntax_template = new File("vlan_acc.syntax");
        try {
            fr.close();
            br.close();
        } catch (Exception e) {
        }
        try {
            String ln = "";
            fr = new FileInputStream(this.vlan_acc_syntax_template);
            br = new BufferedReader(new InputStreamReader(fr));
            while ((ln = br.readLine()) != null) {
                vlan_syntax_template.add(ln);
            }
            try {
                br.close();
                fr.close();
            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.vlan_man_syntax_template = new File("vlan_man.syntax");
        try {
            fr.close();
            br.close();
        } catch (Exception e) {
        }
        try {
            String ln = "";
            fr = new FileInputStream(this.vlan_man_syntax_template);
            br = new BufferedReader(new InputStreamReader(fr));
            while ((ln = br.readLine()) != null) {
                vlan_man_syntax_template.add(ln);
            }
            try {
                br.close();
                fr.close();
            } catch (Exception e) {
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        //configurare acc
           if (role.equals("access")) {
            String interfaces = (String) dev.getEnvVariable("vlan_acc_interfaces").value;
            String[] intrfcs = interfaces.split("~");
            String[] vlans = vlan_acc_name.split(",");
            String[] vlan_numbers = vlan_acc.split(",");
            ArrayList<Variable> proccesed_acc_vlans = new ArrayList<>();
            int index = 0;
            for (String vlan : vlans) {
                HashMap<String, String> value = new HashMap<>();
                value.put("interfaces", intrfcs[index]);
                value.put("vlan_number", vlan_numbers[index]);
                Variable<HashMap<String, String>> vln = new Variable<>(TYPES.VARS.KEY_VALUE, vlan, value);
                proccesed_acc_vlans.add(vln);
                index++;
            }

            proccesed_acc_vlans.forEach(vlan -> {
                HashMap<String, String> params = (HashMap) vlan.value;
                for (int i = 0; i < vlan_syntax_template.size(); i++) {
                    String template_line = new String(vlan_syntax_template.get(i));

                    if (template_line.indexOf("vlan_number") != 0) {
                        template_line = template_line.replaceAll("<<vlan_number>>", params.get("vlan_number"));
                    }

                    if (template_line.indexOf("vlan_name") != 0) {
                        template_line = template_line.replaceAll("<<vlan_name>>", vlan.name);
                    }

                    if (template_line.indexOf("vlan_interface_range") != 0) {
                        template_line = template_line.replaceAll("<<vlan_interface_range>>", params.get("interfaces"));
                    }

                    current_conf.add(template_line);
                }
            });

        } else if (role.equals("trunk")) {
            String[] vlans = vlan_acc_name.split(",");
            String[] vlan_numbers = vlan_acc.split(",");
            ArrayList<Variable> proccesed_acc_vlans = new ArrayList<>();
            int index = 0;
            for (String vlan : vlans) {

                Variable<String> vln = new Variable<>(TYPES.VARS.KEY_VALUE, vlan, vlan_numbers[index]);
                proccesed_acc_vlans.add(vln);
                index++;
            }

            proccesed_acc_vlans.forEach(vlan -> {
                current_conf.add("vlan " + vlan.value);
                current_conf.add("name " + vlan.name);
                current_conf.add("exit");
            });
        }

        //configurare vlan null
        String null_interfaces = (String) dev.getEnvVariable("vlan_null_interfaces").value;
        for (int i = 0; i < vlan_syntax_template.size(); i++) {
            String template_line = new String(vlan_syntax_template.get(i));

            if (template_line.indexOf("vlan_number") != 0) {
                template_line = template_line.replaceAll("<<vlan_number>>", vlan_null);
            }

            if (template_line.indexOf("vlan_name") != 0) {
                template_line = template_line.replaceAll("<<vlan_name>>", "NULL");
            }

            if (template_line.indexOf("vlan_interface_range") != 0) {
                template_line = template_line.replaceAll("<<vlan_interface_range>>", null_interfaces);
            }

            current_conf.add(template_line);
        }
        current_conf.add("shutdown");
        current_conf.add("exit");

     

        //configurare man
        String man_interfaces = (String) dev.getEnvVariable("vlan_man_interfaces").value;
        for (int i = 0; i < vlan_man_syntax_template.size(); i++) {
            String template_line = new String(vlan_man_syntax_template.get(i));

            if (template_line.indexOf("vlan_number") != 0) {
                template_line = template_line.replaceAll("<<vlan_number>>", vlan_trunk);
            }

            if (template_line.indexOf("vlan_name") != 0) {
                template_line = template_line.replaceAll("<<vlan_name>>", "MAN");
            }

            if (template_line.indexOf("vlan_interface_range") != 0) {
                template_line = template_line.replaceAll("<<vlan_interface_range>>", man_interfaces);
            }
            if (template_line.indexOf("allowed_vlans") != 0) {
                template_line = template_line.replaceAll("<<allowed_vlans>>", vlan_acc);
            }

            current_conf.add(template_line);
        }
        current_conf.add("exit");

    }

    private void loadInterfaceConfig(ArrayList<String> current_conf, Device dev) {

    }

    private void loadRoutingConfig(ArrayList<String> current_conf, Device dev) {

    }

    /* public Object extractVarByName(Device dev, String var_name) {
     for (int i = 0; i < dev.environment_vars.size(); i++) {
     Variable var = dev.environment_vars.get(i);
     if (var.name.equals(var_name)) {
     return var.value;
     }
     }
     throw new Exception("Var not found!!");

     }*/
}
