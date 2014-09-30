package com.kryshyna.filecopier;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: vadim
 * Date: 29.09.14
 * Time: 12:38
 * To change this template use File | Settings | File Templates.
 */
public class MainWindow extends JFrame {
    public static void main(String[] args) {
        JFrame frame = new MainWindow();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public MainWindow(){
        super("File copier");
        this.setSize(300, 300);
        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        JLabel labelPartPassword = new JLabel("You can create reserve copy your files:");

        final JTextField textFrom = new JTextField();
        final JTextField textTo = new JTextField();
        final JCheckBox checkZip = new JCheckBox("create copy in zip file");

        JButton buttonFrom = new JButton("from");
        JButton buttonTo = new JButton("to");
        JButton buttonCopy = new JButton("Start copy");

//        final JProgressBar progressBar = new JProgressBar();
//        progressBar.setIndeterminate(true);
//        progressBar.setIndeterminate(false);

        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        menuBar.add(menu);
        this.setJMenuBar(menuBar);
        JMenuItem menuItemAbout = new JMenuItem("About");
        menu.add(menuItemAbout);

        textFrom.setToolTipText("Input source directory");
        textTo.setToolTipText("Input destination directory");
        buttonFrom.setToolTipText("Press for selected source directory");
        buttonTo.setToolTipText("Press for selected destination directory");
        buttonCopy.setToolTipText("Press for start copy");
        checkZip.setToolTipText("Check if need zip copy your files");

        this.setLayout(new GridLayout(7, 1));

        this.add(labelPartPassword);
        this.add(textFrom);
        this.add(buttonFrom);
        this.add(textTo);
        this.add(buttonTo);
        this.add(checkZip);
//        this.add(progressBar);
        this.add(buttonCopy);

//        final SwingWorker worker = new SwingWorker() {
//            @Override
//            protected Object doInBackground() throws Exception {
//                progressBar.setIndeterminate(true);
//                return null;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            protected void done() {
//                progressBar.setIndeterminate(false);
//            }
//
//        };

        menuItemAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, messageAbout);
            }
        });

        buttonFrom.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = fileChooser.showOpenDialog(MainWindow.this);
                if(selected == JFileChooser.APPROVE_OPTION){
                    textFrom.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        buttonTo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selected = fileChooser.showOpenDialog(MainWindow.this);
                if(selected == JFileChooser.APPROVE_OPTION){
                    textTo.setText(fileChooser.getSelectedFile().toString());
                }
            }
        });

        buttonCopy.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //worker.execute();
                repaint();
                if(textFrom.getText().length() == 0 || textTo.getText().length() == 0){
                    JOptionPane.showMessageDialog(null, "Please check selected source and destination");
                }else{
                    fileFrom = new File(textFrom.getText());
                    fileTo = new File(textTo.getText());
                    if (fileFrom.exists()){
                        if(checkZip.isSelected()){
                            File newFileTo = new File(getFileName(fileTo)+".zip");
                            deleteIfExist(newFileTo);
                            try {
                                copyDirectoryToZip(fileFrom, newFileTo);
                                JOptionPane.showMessageDialog(null, "Copier successful complete");
                            } catch (IOException e1) {
                                deleteIfExist(newFileTo);
                                JOptionPane.showMessageDialog(null, "Operation not complete. Please try again");
                            }
                        }else{
                            File newFileTo = new File(getFileName(fileTo));
                            deleteIfExist(newFileTo);
                            try {
                                copyDirectory(fileFrom, newFileTo);
                                JOptionPane.showMessageDialog(null, "Copier successful complete");
                            } catch (IOException e1) {
                                deleteIfExist(newFileTo);
                                JOptionPane.showMessageDialog(null, "Operation not complete. Please try again");
                            }
                        }
                    }else{
                        JOptionPane.showMessageDialog(null, "Wrong source directory");
                    }
                }
            }


        });
    }

    //get new file name
    private String getFileName(File file){
        String strFormat = "yyyy-MM-dd";
        DateFormat dateFormat = new SimpleDateFormat(strFormat);
        String fileCurrentDate = dateFormat.format(new Date());
        return file.toString()+"\\"+fileCurrentDate;
    }

    //delete if exist
    private void deleteIfExist(File file){
        if(file.exists()){
            this.deleteDirectory(file);
        }
    }

    //copy file
    private void copy(File source, File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath());
    }

    //copy directory
    private void copyDirectory(File source, File dest) throws IOException {
        if(source.isDirectory()){
            if(!dest.exists()){
                dest.mkdir();
            }
            String [] listFiles = source.list();
            for(int i = 0; i < listFiles.length; i++){
                copyDirectory(new File(source, listFiles[i]), new File(dest, listFiles[i]));
            }
        }else{
            copy(source, dest);
        }
    }

    //delete directory
    private void deleteDirectory(File directory){
        if(directory.isDirectory()){
            String [] listFiles = directory.list();
            for(int i = 0; i < listFiles.length; i++){
                File file = new File(directory, listFiles[i]);
                deleteDirectory(file);
            }
            directory.delete();
        }else{
            directory.delete();
        }
    }

    //copy directory to zip archive
    private void copyDirectoryToZip(File directory, File zipFile) throws IOException {
        URI base = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);
        OutputStream out = new FileOutputStream(zipFile);
        Closeable res = out;
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            res = zout;
            while (!queue.isEmpty()) {
                directory = queue.pop();
                for (File child : directory.listFiles()) {
                    String name = base.relativize(child.toURI()).getPath();
                    if (child.isDirectory()) {
                        queue.push(child);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        InputStream in = new FileInputStream(child);
                        try {
                            byte[] buffer = new byte[1024];
                            while (true) {
                                int readCount = in.read(buffer);
                                if (readCount < 0) {
                                    break;
                                }
                                zout.write(buffer, 0, readCount);
                            }
                        } finally {
                            in.close();
                        }
                        zout.closeEntry();
                    }
                }
            }
        } finally {
            res.close();
        }
    }

    private File fileFrom = null;
    private File fileTo = null;
    private String messageAbout = "@Author: Vadym Kryshyna \n     2014";
}
