/*
 * Copyright 2014-2015 ieclipse.cn.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.ieclipse.aorm.eclipse.tip;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Enumeration;

/**
 * Preducer
 * 
 * @author Jamling
 *         
 */
public class Preducer {
    
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String path = Preducer.class.getPackage().getName().replaceAll("\\.", "/");
        File base = new File("src/" + path);
        // File f = new File(Preducer.class.getResource(".").getPath());
        System.out.println(base);
        File[] fs = base.listFiles();
        for (File t : fs) {
            if (t.getName().endsWith(".html")) {
                preduce(t);
            }
        }
    }
    
    static void preduce(File f) throws Exception {
        File out = new File(f.getParentFile(),
                f.getName().replace(".html", ".xml"));

        System.out.println("preduc " + f + " to " + out);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(out), Charset.forName("utf-8")));
        
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(f)));
        String line = br.readLine();
        while (line != null) {
            String newLine = line.replaceAll("<br>", "<br/>").replaceAll("&nbsp;", " ");
            bw.write(newLine);
            bw.write("\r\n");
            line = br.readLine();
        }
        bw.flush();
        br.close();
        bw.close();
    }
    
}
