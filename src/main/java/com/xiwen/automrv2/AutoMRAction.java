package com.xiwen.automrv2;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动合并插件入口
 */
public class AutoMRAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {

        MyConfigService myConfigService = new MyConfigService();

        // 显示自定义操作页面
        AutoMRDialog dialog = new AutoMRDialog();
        if (dialog.showAndGet()) {
            // 获取用户输入的参数
            String projectName = dialog.getProjectName();
            String sourceBranch = dialog.getSourceBranch();

            List<String> targetBranches = dialog.getTargetBranches();
            String mrTitle = dialog.getMrTitle();

            GitLabService gitLabService = new GitLabService();

            try {
                gitLabService.createMergeRequest(projectName, sourceBranch, targetBranches, mrTitle);
                StringBuilder message = new StringBuilder();
                message.append("项目: ").append(projectName).append("\n");
                message.append("源分支: ").append(sourceBranch).append("\n");
                message.append("目标分支: ").append(String.join(", ", targetBranches)).append("\n");
                message.append("MR标题: ").append(mrTitle);

                //把用户自定义的源分支保存到配置文件中
                myConfigService.writeConfig("SOURCE_BRANCH", sourceBranch);

                Messages.showInfoMessage(message.toString(), "自动提交合并请求任务已完成!");
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static class AutoMRDialog extends DialogWrapper {
        private JComboBox<String> projectNameCombo;
        private JTextField sourceBranchField;
        private JPanel targetBranchPanel; // 用于放置多个复选框
        private JTextField mrTitleField;
        private JTextField configFilePathField; // Replace configButton with textField
        private JButton browseConfigButton; // A

        public AutoMRDialog() {
            super(true);
            init();
            setTitle("自动创建合并请求");

            String savedConfigPath = System.getProperty("AUTOMR_CONFIG_PATH");
            if (savedConfigPath != null && !savedConfigPath.isEmpty()) {
                configFilePathField.setText(savedConfigPath);
            }
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.WEST;

            gbc.gridx = 0;
            gbc.gridy = 4;
            panel.add(new JLabel("配置文件:"), gbc);
            gbc.gridx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            JPanel configPanel = new JPanel(new BorderLayout());
            configFilePathField = new JTextField(20);
            browseConfigButton = new JButton("浏览");
            browseConfigButton.addActionListener(e -> browseConfigFile());
            configPanel.add(configFilePathField, BorderLayout.CENTER);
            configPanel.add(browseConfigButton, BorderLayout.EAST);
            panel.add(configPanel, gbc);

            MyConfigService myConfigService = new MyConfigService();
            String sourceBranch = myConfigService.readConfigByKey("SOURCE_BRANCH");
            List<String> projectOptions = myConfigService.readConfigListByKey("PROJECT_OPTIONS");

            // 第一行 - 项目名称（下拉框）
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(new JLabel("项目名称:"), gbc);
            gbc.gridx = 1;
            projectNameCombo = new ComboBox<>(projectOptions.toArray(new String[0]));
            projectNameCombo.setSelectedItem("pm-project-middle-end");
            panel.add(projectNameCombo, gbc);

            // 第二行 - 源分支
            gbc.gridx = 0;
            gbc.gridy = 1;
            panel.add(new JLabel("源分支:"), gbc);
            gbc.gridx = 1;
            sourceBranchField = new JTextField(sourceBranch, 20);
            panel.add(sourceBranchField, gbc);

            // 第三行 - 目标分支（多选框面板）
            gbc.gridx = 0;
            gbc.gridy = 2;
            panel.add(new JLabel("目标分支:"), gbc);
            gbc.gridx = 1;
            targetBranchPanel = createTargetBranchCheckBoxes();
            panel.add(targetBranchPanel, gbc);

            // 第四行 - MR标题
            gbc.gridx = 0;
            gbc.gridy = 3;
            panel.add(new JLabel("MR标题:"), gbc);
            gbc.gridx = 1;
            mrTitleField = new JTextField("AutoMR", 20);
            panel.add(mrTitleField, gbc);

            return panel;
        }

        private void browseConfigFile() {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("选择配置文件");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

            fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));

            fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f.isDirectory() || f.getName().toLowerCase().endsWith(".json") ||
                            f.getName().toLowerCase().endsWith(".properties") ||
                            f.getName().toLowerCase().endsWith(".yaml") ||
                            f.getName().toLowerCase().endsWith(".yml");
                }

                @Override
                public String getDescription() {
                    return "Configuration Files (*.json, *.properties, *.yaml, *.yml)";
                }
            });

            int result = fileChooser.showOpenDialog(this.getWindow());
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                String configPath = selectedFile.getAbsolutePath();
                configFilePathField.setText(configPath);
                System.setProperty("AUTOMR_CONFIG_PATH", configPath);
            }
        }

        // 创建目标分支复选框面板
        private JPanel createTargetBranchCheckBoxes() {
            JPanel checkBoxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

            MyConfigService myConfigService = new MyConfigService();
            List<String> targetBranchOptions = myConfigService.readConfigListByKey("TARGET_BRANCH_OPTIONS");

            for (String branch : targetBranchOptions) {
                JCheckBox checkBox = new JCheckBox(branch);
                if ("develop".equals(branch)) {
                    checkBox.setSelected(true); // 默认选中develop分支
                }
                checkBoxPanel.add(checkBox);
            }
            return checkBoxPanel;
        }

        public String getProjectName() {
            return (String) projectNameCombo.getSelectedItem();
        }

        public String getSourceBranch() {
            return sourceBranchField.getText();
        }

        // 获取选中的目标分支列表
        public List<String> getTargetBranches() {
            List<String> selectedBranches = new ArrayList<>();
            Component[] components = targetBranchPanel.getComponents();
            for (Component component : components) {
                if (component instanceof JCheckBox) {
                    JCheckBox checkBox = (JCheckBox) component;
                    if (checkBox.isSelected()) {
                        selectedBranches.add(checkBox.getText());
                    }
                }
            }
            return selectedBranches;
        }

        public String getMrTitle() {
            return mrTitleField.getText();
        }
    }
}
