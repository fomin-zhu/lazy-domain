package com.fomin.plugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * @author fomin
 * @since 2020/6/16
 */
public class EntityAutomaticCreateAction extends AnAction {
    private Project project;
    private String packageName;
    private String authorName;
    private String entityName;

    @Override
    public void actionPerformed(AnActionEvent e) {
        project = e.getData(PlatformDataKeys.PROJECT);
        packageName = getPackageName(e);
        init();
        refreshProject(e);
    }

    private String getPackageName(AnActionEvent e) {
        VirtualFile virtualFile = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (!virtualFile.isDirectory()) {
            virtualFile = virtualFile.getParent();
        }
        Module module = ModuleUtil.findModuleForFile(virtualFile, project);
        String moduleRootPath = ModuleRootManager.getInstance(module).getContentRoots()[0].getPath();
        String actionDir = virtualFile.getPath();
        String str = StringUtils.substringAfter(actionDir, moduleRootPath + "/src/main/java/");
        return str.replace("/", ".");
    }

    /**
     * 刷新项目
     *
     * @param e
     */
    private void refreshProject(AnActionEvent e) {
        Project project = e.getProject();
        if (Objects.isNull(project)) {
            return;
        }
        VirtualFile file = project.getProjectFile();
        if (Objects.isNull(file)) {
            return;
        }
        file.refresh(false, true);
    }

    /**
     * 初始化Dialog
     */
    private void init() {
        EntityAutomaticCreate dialog = new EntityAutomaticCreate((author, fileName, type) -> {
            authorName = author;
            entityName = fileName;
            if (StringUtils.isBlank(entityName)) {
                return;
            }
            createEntityFile(type);
            Messages.showInfoMessage(project, "创建领域相关类成功", "创建");
        });
        dialog.setVisible(true);
    }

    /**
     * 生成mvp框架代码
     */
    private void createEntityFile(List<EntityType> types) {
        types.forEach(it -> {
            CreateParam param = buildParam(it);
            if (param != null) {
                String content = readTemplateFile(param.tempName);
                content = dealTemplateContent(content);
                writeToFile(content, getPackagePath() + param.path, param.fileName);
            }
        });

    }

    private CreateParam buildParam(EntityType type) {
        switch (type) {
            case ENTITY:
                return new CreateParam("EntityTemp.txt", "domain/entity/", entityName + ".java");
            case BASE:
                return new CreateParam("DomainEntity.txt", "domain/entity/", "BaseEntity.java");
            case BUILDER:
                return new CreateParam("Builder.txt", "domain/", "Builder.java");
            case DO:
                return new CreateParam("DoTemp.txt", "model/entity/", entityName + "DO.java");
            case DTO:
                return new CreateParam("DtoTemp.txt", "model/dto/", entityName + "DTO.java");
            case BO:
                return new CreateParam("BoTemp.txt", "model/bo/", entityName + "BO.java");
            case VO:
                return new CreateParam("VoTemp.txt", "model/vo/", entityName + "VO.java");
            case QUERY:
                return new CreateParam("QueryTemp.txt", "model/query/", entityName + "Query.java");
            case MAPPER:
                return new CreateParam("MapperTemp.txt", "mapper/", entityName + "Mapper.java");
            case REPO:
                return new CreateParam("RepoTemp.txt", "repo/", entityName + "Repository.java");
            case MANAGER:
                return new CreateParam("ManagerTemp.txt", "domain/manager/", entityName + "Manager.java");
            case SERVICE:
                return new CreateParam("ServiceTemp.txt", "service/", entityName + "Service.java");
            case CONTROLLER:
                return new CreateParam("ControllerTemp.txt", "controller/", entityName + "Controller.java");
            default:
                return null;
        }
    }

    /**
     * 读取模板文件中的字符内容
     *
     * @param fileName 模板文件名
     */
    private String readTemplateFile(String fileName) {
        InputStream in = this.getClass().getResourceAsStream("template/" + fileName);
        String content = "";
        try {
            content = new String(readStream(in));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }


    /**
     * 读取数据
     *
     * @param inputStream 输入流
     */
    private byte[] readStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            outputStream.close();
            inputStream.close();
        }

        return outputStream.toByteArray();
    }

    /**
     * 生成
     *
     * @param content   类中的内容
     * @param classPath 类文件路径
     * @param className 类文件名称
     */
    private void writeToFile(String content, String classPath, String className) {
        try {
            File folder = new File(classPath);
            if (!folder.exists()) {
                folder.mkdirs();
            }

            File file = new File(classPath + "/" + className);
            if (file.exists()) {
                return;
            }
            file.createNewFile();
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 替换模板中字符
     *
     * @param content 内容
     * @return 文件内容
     */
    private String dealTemplateContent(String content) {
        content = content.replace("$packageName", packageName);
        content = content.replace("$author", authorName);
        content = content.replace("$date", getDate());
        content = content.replace("$fileName", entityName);
        content = content.replace("$tableName", entityName.toLowerCase());
        return content;
    }

    /**
     * 获取包名文件路径
     *
     * @return 包名
     */
    private String getPackagePath() {
        String packagePath = packageName.replace(".", "/");
        return project.getBasePath() + "/src/main/java/" + packagePath + "/";
    }

    /**
     * 获取当前时间
     *
     * @return 字符串时间
     */
    public String getDate() {
        LocalDate currentTime = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return formatter.format(currentTime);
    }

    public static class CreateParam {
        private String tempName;
        private String path;
        private String fileName;

        public CreateParam(String tempName, String path, String fileName) {
            this.tempName = tempName;
            this.path = path;
            this.fileName = fileName;
        }

        public String getTempName() {
            return tempName;
        }

        public String getPath() {
            return path;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
