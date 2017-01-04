package me.leefeng;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import groovy.util.logging.Log;
import groovy.util.logging.Log4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

/**
 * Created by FengTing on 2017/1/4.
 */
public class NewActivity extends AnAction {

    private Project project;
    private String packageName;

    @Override
    public void actionPerformed(AnActionEvent e) {
        // TODO: insert action logic here
        Map<String, String> map = System.getenv();
        String userName = map.get("USERNAME");// 获取用户名

        project = e.getData(PlatformDataKeys.PROJECT);
        String name = Messages.showInputDialog(project, "起个名字，一定要吊炸天的名字！", "先想个名字",
                Messages.getQuestionIcon());
        if (name == null || name.trim().length() == 0) {
            Messages.showInfoMessage(project, "不能为空哦！！", "提示");
            name = Messages.showInputDialog(project, "起个名字，一定要吊炸天的名字！", "先想个名字",
                    Messages.getQuestionIcon());
            return;
        }
        String smallname = toUpperOrNot(name, false);
        String bigname = toUpperOrNot(name, true);
        packageName = readPackageName();
        String s = new String(packageName).replace(".", "/");
        String basePath = project.getBasePath() + "/app/src/main/java/" + s;
        File baseActivity = new File(basePath + "/BaseActivity.java");

        if (!baseActivity.exists()) {
            File[] baseFiles = new File(this.getClass().getResource("/Base").getPath()).listFiles();
            for (File file : baseFiles) {
                InputStream inputStream;
                String content = "";
                try {
                    inputStream = new FileInputStream(file);
                    content = new String(readStream(inputStream));
                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                content = content.replace("$packagename", packageName);
                content = content.replace("$date", getNowDateShort());
                content = content.replace("$author", userName);
                writetoFile(content, basePath, file.getName().replace("txt", "java"));
            }
            manifast(".ProjectApplication", null);
        }
        File newFile = new File(basePath, name);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }

        File[] baseFiles = new File(this.getClass().getResource("/Activity").getPath()).listFiles();
        for (File file : baseFiles) {
            InputStream inputStream;
            String content = "";
            try {
                inputStream = new FileInputStream(file);
                content = new String(readStream(inputStream));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            content = content.replace("$packagename", packageName);
            content = content.replace("$date", getNowDateShort());
            content = content.replace("$smallname", smallname);
            content = content.replace("$name", bigname);
            content = content.replace("$author", userName);
            writetoFile(content, basePath + "/" + smallname, bigname + file.getName().replace("txt", "java"));
        }
        File layoutFile = new File(this.getClass().getResource("/Layout/activity_.txt").getPath());
        InputStream inputStream;
        String content = "";
        try {
            inputStream = new FileInputStream(layoutFile);
            content = new String(readStream(inputStream));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        writetoFile(content, project.getBasePath() + "/app/src/main/res/layout",
                layoutFile.getName().replace(".txt", smallname + ".xml"));

        manifast(null, "." + smallname + "." + bigname + "Activity");
        Messages.showInfoMessage(project, "成功添加，请刷新列表", "提示");
    }


    /**
     * 转换成大写还是小写
     *
     * @param s 字符串
     * @param b
     * @return
     */
    private String toUpperOrNot(String s, boolean b) {
        char[] cs = s.toCharArray();
        char c = cs[0];
        boolean isSmall = false;
        if (c >= 'a' && c <= 'z') {
            isSmall = true;
        }
        if (b) {
            //转换为大写
            if (isSmall) {
                cs[0] -= 32;
            }
        } else {
            if (!isSmall) {
                cs[0] += 32;
            }
        }
        return String.valueOf(cs);
    }

    private void writetoFile(String content, String filepath, String filename) {
        try {
            File floder = new File(filepath);
            // if file doesnt exists, then create it
            if (!floder.exists()) {
                floder.mkdirs();
            }
            File file = new File(filepath + "/" + filename);
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 获取时间
     *
     * @return
     */
    public String getNowDateShort() {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String dateString = formatter.format(currentTime);
        return dateString;
    }


    private String ReadFile(String filename) {
        InputStream in = null;
        in = this.getClass().getResourceAsStream("/Template/" + filename);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (Exception e) {
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    /**
     * 流读出字节数组
     *
     * @param inStream
     * @return
     * @throws Exception
     */
    public byte[] readStream(InputStream inStream) throws Exception {
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        try {
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = inStream.read(buffer)) != -1) {
                outSteam.write(buffer, 0, len);
//                System.out.println(new String(buffer));
            }

        } catch (IOException e) {
        } finally {
            outSteam.close();
            inStream.close();
        }
        return outSteam.toByteArray();
    }

    /**
     * 读取package
     *
     * @return
     */
    private String readPackageName() {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/app/src/main/AndroidManifest.xml");
            NodeList list = doc.getElementsByTagName("application");
            for (int i = 0; i < list.getLength(); i++) {
                Node dog = list.item(i);
                Element elem = (Element) dog;
                System.out.println(elem.getTagName());
            }


            NodeList dogList = doc.getElementsByTagName("manifest");
            for (int i = 0; i < dogList.getLength(); i++) {
                Node dog = dogList.item(i);
                Element elem = (Element) dog;
                return elem.getAttribute("package");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {

        }
        return "";
    }

    private void manifast(String name, String activity) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(project.getBasePath() + "/app/src/main/AndroidManifest.xml");
            NodeList list = doc.getElementsByTagName("application");
            for (int i = 0; i < list.getLength(); i++) {
                Node dog = list.item(i);
                Element elem = (Element) dog;
                if (activity == null) {
                    if (elem.getAttribute("android:name") != null) {
                        elem.removeAttribute("android:name");
                    }
                    elem.setAttribute("android:name", name);
                } else {
                    Element methodElement = doc.createElement("activity");
                    methodElement.setAttribute("android:name", activity);
                    methodElement.setAttribute("android:configChanges", "orientation|keyboardHidden");
                    methodElement.setAttribute("android:windowSoftInputMode", "adjustPan|stateHidden");
                    elem.appendChild(methodElement);
                }
                break;
            }
            new File(project.getBasePath() + "/app/src/main/AndroidManifest.xml").delete();
            //开始把Document映射到文件
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();

            //设置输出结果
            DOMSource domSource = new DOMSource(doc);
            //生成xml文件
            File file = new File(project.getBasePath() + "/app/src/main/AndroidManifest.xml");
            //判断是否存在,如果不存在,则创建
            if (!file.exists()) {
                file.createNewFile();
            }
            //文件输出流
            FileOutputStream out = new FileOutputStream(file);
            //设置输入源
            StreamResult xmlResult = new StreamResult(out);
            //输出xml文件
            transFormer.transform(domSource, xmlResult);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
