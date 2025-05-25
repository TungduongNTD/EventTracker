package client.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.toedter.calendar.JDateChooser;

import common.model.Event;

public class AddEventDialog extends JDialog {
    private JTextField nameField;
    private JDateChooser dateChooser;
    private JComboBox<String> typeComboBox;
    private JTextField descriptionField;
    private JButton submitButton;
    private JButton cancelButton;

    private boolean submitted = false;
    private Event event;
    private Event originalEvent = null;

    public AddEventDialog(JFrame parent, boolean modal, Event event) {
        super(parent, "Edit Event", modal);
        initializeUI();
        if (event != null) {
            originalEvent = event;
            nameField.setText(event.getTitle());
            if (dateChooser != null && event.getStartTime() != null) {
                java.util.Date date = java.util.Date.from(event.getStartTime().atZone(java.time.ZoneId.systemDefault()).toInstant());
                dateChooser.setDate(date);
            }
            typeComboBox.setSelectedItem(event.getLocation());
            descriptionField.setText(event.getDescription());
        }
    }

    public AddEventDialog(JFrame parent, boolean modal) {
        this(parent, modal, null);
    }

    private void initializeUI() {
        setSize(400, 300);
        setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 5, 5));

        formPanel.add(new JLabel("Event Name:"));
        nameField = new JTextField();
        formPanel.add(nameField);

        formPanel.add(new JLabel("Event Date:"));
        dateChooser = new JDateChooser();
        formPanel.add(dateChooser);

        formPanel.add(new JLabel("Event Type:"));
        String[] types = {"Birthday", "Anniversary", "Holiday", "Meeting", "Other"};
        typeComboBox = new JComboBox<>(types);
        formPanel.add(typeComboBox);

        formPanel.add(new JLabel("Description:"));
        descriptionField = new JTextField();
        formPanel.add(descriptionField);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("Submit");
        cancelButton = new JButton("Cancel");

        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Event listeners
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (validateInput()) {
                    if (originalEvent != null) {
                        event = new Event();
                        event.setId(originalEvent.getId());
                    } else {
                        event = new Event();
                    }
                    event.setTitle(nameField.getText());
                    event.setStartTime(dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    event.setEndTime(dateChooser.getDate().toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime());
                    event.setLocation((String) typeComboBox.getSelectedItem());
                    event.setDescription(descriptionField.getText());
                    submitted = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(AddEventDialog.this,
                            "Please fill all fields", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private boolean validateInput() {
        return !nameField.getText().isEmpty()
                && dateChooser.getDate() != null
                && !descriptionField.getText().isEmpty();
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public Event getEvent() {
        return event;
    }
}