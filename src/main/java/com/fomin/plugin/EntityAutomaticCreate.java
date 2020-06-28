package com.fomin.plugin;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author fomin
 */
public class EntityAutomaticCreate extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField txtAuthor;
    private JTextField txtEntity;
    private JCheckBox chkDo;
    private JCheckBox ckVo;
    private JCheckBox ckQuery;
    private JCheckBox ckBo;
    private JCheckBox chkDto;
    private JCheckBox ckRepo;
    private JCheckBox ckManager;
    private JCheckBox ckController;
    private JCheckBox ckMapper;
    private JCheckBox ckService;

    private DialogCallBack callBack;

    public EntityAutomaticCreate(DialogCallBack callBack) {
        this.callBack = callBack;
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);
        setSize(500, 300);
        setLocationRelativeTo(null);

        buttonOK.addActionListener(e -> onOK());
        buttonCancel.addActionListener(e -> onCancel());
        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        if (null != callBack) {
            callBack.ok(txtAuthor.getText().trim(), txtEntity.getText().trim(), getEntity());
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private List<EntityType> getEntity() {
        List<EntityType> types = new ArrayList<>();
        types.add(EntityType.ENTITY);
        types.add(EntityType.BASE);
        types.add(EntityType.BUILDER);
        if (chkDo.isSelected()) {
            types.add(EntityType.DO);
        }
        if (chkDto.isSelected()) {
            types.add(EntityType.DTO);
        }
        if (ckBo.isSelected()) {
            types.add(EntityType.BO);
        }
        if (ckVo.isSelected()) {
            types.add(EntityType.VO);
        }
        if (ckQuery.isSelected()) {
            types.add(EntityType.QUERY);
        }
        if (ckManager.isSelected()) {
            types.add(EntityType.MANAGER);
        }
        if (ckMapper.isSelected()) {
            types.add(EntityType.MAPPER);
        }
        if (ckRepo.isSelected()) {
            types.add(EntityType.REPO);
        }
        if (ckService.isSelected()) {
            types.add(EntityType.SERVICE);
        }
        if (ckController.isSelected()) {
            types.add(EntityType.CONTROLLER);
        }
        return types;
    }

    public interface DialogCallBack {
        /**
         * 确定
         *
         * @param author   作者
         * @param fileName 文件名称
         * @param type     类型
         */
        void ok(String author, String fileName, List<EntityType> type);
    }

}
