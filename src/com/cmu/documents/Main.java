package com.cmu.documents;

import java.awt.*;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/** Self-contained Swing application for CMU document requests. */
public final class Main {
    static final Color NAVY = new Color(24, 51, 91), GOLD = new Color(221, 166, 48);
    static final Color BACKGROUND = new Color(246, 248, 252);
    static final String[] DEPARTMENTS = {"CCS", "CTE", "CAS", "CCJE", "CBA", "COA"};
    static final String[] DOCUMENTS = {"TOR", "Diploma", "Birth Certificate", "Honorable Dismissal", "Good Moral"};
    static final String[] STATUSES = {"Pending", "Accepted", "Ready for Pickup", "Completed", "Rejected"};
    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a");
    static final File STORE = new File(System.getProperty("user.home"), ".cmu-document-requests.dat");
    static final Data DATA = Data.load();
    static JFrame frame;
    static User current;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("City of Malabon University | Document Requests");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setMinimumSize(new Dimension(900, 640));
            showView(new LoginView());
            frame.setVisible(true);
        });
    }

    static void showView(JPanel panel) { frame.setContentPane(panel); frame.revalidate(); frame.repaint(); }
    static void message(String text) { JOptionPane.showMessageDialog(frame, text); }
    static JButton button(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false); b.setBackground(NAVY); b.setForeground(Color.WHITE);
        b.setBorder(new EmptyBorder(9, 16, 9, 16)); return b;
    }
    static JLabel heading(String text) {
        JLabel l = new JLabel(text); l.setForeground(NAVY); l.setFont(new Font("SansSerif", Font.BOLD, 25)); return l;
    }
    static JPanel card() {
        JPanel p = new JPanel(new GridBagLayout()); p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(220, 225, 235)), new EmptyBorder(24, 32, 24, 32)));
        return p;
    }
    static GridBagConstraints constraints() {
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(7, 7, 7, 7);
        c.fill = GridBagConstraints.HORIZONTAL; c.anchor = GridBagConstraints.WEST; return c;
    }
    static void field(JPanel p, GridBagConstraints c, String label, JComponent input) {
        c.gridx = 0; c.gridwidth = 1; p.add(new JLabel(label), c); c.gridx = 1; c.weightx = 1; p.add(input, c);
    }
    static JComboBox<String> departments() { return new JComboBox<>(DEPARTMENTS); }
    static JPanel page(String title) {
        JPanel root = new JPanel(new BorderLayout(0, 14)); root.setBackground(BACKGROUND);
        root.setBorder(new EmptyBorder(18, 24, 20, 24)); root.add(new Header(), BorderLayout.NORTH);
        JPanel body = new JPanel(new BorderLayout(0, 14)); body.setOpaque(false); body.add(heading(title), BorderLayout.NORTH);
        root.add(body, BorderLayout.CENTER); return root;
    }
    static JPanel bodyOf(JPanel page) { return (JPanel) page.getComponent(1); }

    static final class Header extends JPanel {
        Header() {
            setLayout(new BorderLayout()); setOpaque(false); add(new Logo(), BorderLayout.WEST);
            if (current != null) {
                JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT)); actions.setOpaque(false);
                actions.add(new JLabel((current.admin ? "Admin · " : "Student · ") + current.id));
                JButton about = button("About Us"), contact = button("Contact Us"), signOut = button("Sign out");
                about.addActionListener(e -> showView(new AboutView())); contact.addActionListener(e -> showView(new ContactView()));
                signOut.addActionListener(e -> { current = null; showView(new LoginView()); });
                actions.add(about); actions.add(contact); actions.add(signOut); add(actions, BorderLayout.EAST);
            }
        }
    }
    static final class Logo extends JPanel {
        Logo() { setOpaque(false); setPreferredSize(new Dimension(350, 58)); }
        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics); Graphics2D g = (Graphics2D) graphics;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(NAVY); g.fillOval(3, 4, 48, 48); g.setColor(GOLD); g.fillOval(11, 12, 32, 32);
            g.setColor(Color.WHITE); g.setFont(new Font("SansSerif", Font.BOLD, 17)); g.drawString("CMU", 11, 34);
            g.setColor(NAVY); g.setFont(new Font("SansSerif", Font.BOLD, 15)); g.drawString("CITY OF MALABON UNIVERSITY", 61, 25);
            g.setFont(new Font("SansSerif", Font.PLAIN, 11)); g.drawString("ONLINE DOCUMENT REQUESTING SYSTEM", 61, 42);
        }
    }

    static final class LoginView extends JPanel {
        LoginView() {
            setLayout(new GridBagLayout()); setBackground(BACKGROUND); JPanel c = card(); GridBagConstraints gc = constraints();
            gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; c.add(new Logo(), gc); gc.gridy++;
            c.add(heading("Welcome"), gc); gc.gridy++; c.add(new JLabel("Choose your account"), gc); gc.gridy++;
            JButton student = button("Student"); JButton admin = button("Admin"); JPanel roles = new JPanel(); roles.setOpaque(false);
            roles.add(student); roles.add(admin); c.add(roles, gc); student.addActionListener(e -> showView(new StudentAuth(false)));
            admin.addActionListener(e -> showView(new AdminAuth(false))); add(c);
        }
    }
    static final class StudentAuth extends JPanel {
        StudentAuth(boolean signup) {
            setLayout(new GridBagLayout()); setBackground(BACKGROUND); JPanel c = card(); GridBagConstraints gc = constraints();
            gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; c.add(heading(signup ? "Student Sign Up" : "Student Login"), gc); gc.gridy++;
            JTextField id = new JTextField(20), password = new JTextField(20); JComboBox<String> dept = departments();
            field(c, gc, "Student ID number *", id); gc.gridy++; field(c, gc, "Password *", password); gc.gridy++; field(c, gc, "Department *", dept); gc.gridy++;
            JButton submit = button(signup ? "Create account" : "Log in"), back = button("Back"); JPanel actions = new JPanel(); actions.setOpaque(false);
            actions.add(submit); actions.add(back); c.add(actions, gc); gc.gridy++;
            JButton link = new JButton(signup ? "Already registered? Log in" : "New student? Sign up"); link.setBorderPainted(false); link.setContentAreaFilled(false); c.add(link, gc);
            submit.addActionListener(e -> {
                String key = id.getText().trim(), pw = password.getText();
                if (key.isEmpty() || pw.isEmpty()) { message("Complete all required fields."); return; }
                String d = (String) dept.getSelectedItem();
                if (signup) { if (DATA.students.containsKey(key)) { message("Student ID already exists."); return; }
                    DATA.students.put(key, new User(key, pw, d, false)); DATA.save(); message("Account created. Please log in."); showView(new StudentAuth(false));
                } else { User u = DATA.students.get(key); if (u == null || !u.password.equals(pw) || !u.department.equals(d)) { message("Invalid ID, password, or department."); return; }
                    current = u; showView(new StudentHome());
                }
            });
            back.addActionListener(e -> showView(new LoginView())); link.addActionListener(e -> showView(new StudentAuth(!signup))); add(c);
        }
    }
    static final class AdminAuth extends JPanel {
        AdminAuth(boolean signup) {
            setLayout(new GridBagLayout()); setBackground(BACKGROUND); JPanel c = card(); GridBagConstraints gc = constraints();
            gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2; c.add(heading(signup ? "Admin Sign Up" : "Admin Login"), gc); gc.gridy++;
            JTextField id = new JTextField(20), password = new JTextField(20); JComboBox<String> dept = departments();
            field(c, gc, "Admin ID number *", id); gc.gridy++; field(c, gc, "Password *", password); gc.gridy++; field(c, gc, "Department *", dept); gc.gridy++;
            JButton submit = button(signup ? "Create admin" : "Log in"), back = button("Back"); JPanel actions = new JPanel(); actions.setOpaque(false);
            actions.add(submit); actions.add(back); c.add(actions, gc); gc.gridy++; JButton link = new JButton(signup ? "Already registered? Log in" : "New admin? Sign up");
            link.setBorderPainted(false); link.setContentAreaFilled(false); c.add(link, gc);
            submit.addActionListener(e -> {
                String key = id.getText().trim(), pw = password.getText(), d = (String) dept.getSelectedItem();
                if (key.isEmpty() || pw.isEmpty()) { message("Complete all required fields."); return; }
                if (signup) { if (DATA.admins.containsKey(key)) { message("Admin ID already exists."); return; }
                    DATA.admins.put(key, new User(key, pw, d, true)); DATA.save(); message("Admin account created. Please log in."); showView(new AdminAuth(false));
                } else { User u = DATA.admins.get(key); if (u == null || !u.password.equals(pw) || !u.department.equals(d)) { message("Invalid ID, password, or department."); return; }
                    current = u; showView(new AdminHome());
                }
            });
            back.addActionListener(e -> showView(new LoginView())); link.addActionListener(e -> showView(new AdminAuth(!signup))); add(c);
        }
    }

    static final class StudentHome extends JPanel {
        StudentHome() {
            JPanel root = page("Student Dashboard"), body = bodyOf(root); JPanel content = new JPanel(new BorderLayout(0, 12)); content.setOpaque(false);
            JLabel reminder = new JLabel("<html><b>Reminder:</b> Please avoid false information and typos in your identification.</html>");
            reminder.setForeground(new Color(145, 65, 30)); content.add(reminder, BorderLayout.NORTH);
            JPanel actions = new JPanel(new GridLayout(1, 2, 18, 0)); actions.setOpaque(false); JButton request = button("Request a Document"), history = button("My Submitted Requests");
            actions.add(request); actions.add(history); content.add(actions, BorderLayout.CENTER); body.add(content, BorderLayout.CENTER);
            request.addActionListener(e -> showView(new RequestView())); history.addActionListener(e -> showView(new HistoryView())); setLayout(new BorderLayout()); add(root);
        }
    }
    static final class RequestView extends JPanel {
        RequestView() {
            JPanel root = page("Request a Document"), body = bodyOf(root); JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE);
            form.setBorder(new EmptyBorder(15, 22, 15, 22)); GridBagConstraints gc = constraints();
            JTextField name = new JTextField(28), email = new JTextField(28), course = new JTextField(28), contact = new JTextField(28);
            JComboBox<String> dept = departments(), doc = new JComboBox<>(DOCUMENTS);
            field(form, gc, "Full name *", name); gc.gridy++; field(form, gc, "Email *", email); gc.gridy++; field(form, gc, "Course and section *", course);
            gc.gridy++; field(form, gc, "Contact number *", contact); gc.gridy++; field(form, gc, "Department *", dept); gc.gridy++; field(form, gc, "Document *", doc); gc.gridy++;
            JButton submit = button("Submit request"), back = button("Back"); JPanel actions = new JPanel(); actions.setOpaque(false); actions.add(submit); actions.add(back); form.add(actions, gc);
            body.add(new JScrollPane(form), BorderLayout.CENTER);
            submit.addActionListener(e -> {
                String n = name.getText().trim(), em = email.getText().trim(), co = course.getText().trim(), phone = contact.getText().trim();
                if (n.isEmpty() || em.isEmpty() || co.isEmpty() || phone.isEmpty()) { message("Complete all required fields."); return; }
                if (!em.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) { message("Enter a valid email address."); return; }
                if (!phone.matches("^[0-9+()\\-\\s]{7,20}$")) { message("Enter a valid contact number."); return; }
                Request r = new Request(UUID.randomUUID().toString(), current.id, n, em, co, phone, (String) dept.getSelectedItem(), (String) doc.getSelectedItem());
                DATA.requests.add(r); DATA.save(); showView(new ReceiptView(r));
            });
            back.addActionListener(e -> showView(new StudentHome())); setLayout(new BorderLayout()); add(root);
        }
    }
    static final class ReceiptView extends JPanel {
        ReceiptView(Request r) {
            JPanel root = page("Request Receipt"), body = bodyOf(root); JTextArea receipt = new JTextArea();
            receipt.setEditable(false); receipt.setFont(new Font("Monospaced", Font.PLAIN, 14)); receipt.setBorder(new EmptyBorder(18, 25, 18, 25));
            receipt.setText("CITY OF MALABON UNIVERSITY\nONLINE DOCUMENT REQUEST RECEIPT\n\nRequest ID: " + r.id + "\nFull name: " + r.name + "\nEmail: " + r.email
                + "\nContact number: " + r.contact + "\nCourse and section: " + r.course + "\nDepartment: " + r.department + "\nRequested document: " + r.document
                + "\nRequest date/time: " + r.created + "\nCurrent status: " + r.status + "\n\nScreenshot your receipt.");
            body.add(new JScrollPane(receipt), BorderLayout.CENTER); JButton done = button("Go to dashboard"); body.add(done, BorderLayout.SOUTH);
            done.addActionListener(e -> showView(new StudentHome())); setLayout(new BorderLayout()); add(root);
        }
    }
    static final class HistoryView extends JPanel {
        HistoryView() {
            JPanel root = page("My Submitted Requests"), body = bodyOf(root);
            JTable table = new JTable(rowsFor(current.id), new String[]{"ID", "Document", "Date", "Status", "Pickup month / date / time"});
            table.setAutoCreateRowSorter(true); table.setFillsViewportHeight(true); body.add(new JScrollPane(table), BorderLayout.CENTER);
            JButton back = button("Back"); body.add(back, BorderLayout.SOUTH); back.addActionListener(e -> showView(new StudentHome())); setLayout(new BorderLayout()); add(root);
        }
    }
    static Object[][] rowsFor(String student) {
        java.util.List<Request> list = new ArrayList<>(); for (Request r : DATA.requests) if (r.studentId.equals(student)) list.add(r);
        Object[][] rows = new Object[list.size()][5]; for (int i = 0; i < list.size(); i++) { Request r = list.get(i); rows[i] = new Object[]{r.id.substring(0, 8), r.document, r.created, r.status, r.pickup()}; } return rows;
    }

    static final class AdminHome extends JPanel {
        JTable table; JComboBox<String> filter; java.util.List<Request> visible = new ArrayList<>();
        AdminHome() {
            JPanel root = page("Admin Department Dashboard"), body = bodyOf(root); JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); top.setOpaque(false);
            top.add(new JLabel("Department:")); filter = new JComboBox<>(new String[]{current.department}); top.add(filter); body.add(top, BorderLayout.NORTH);
            table = new JTable(new Object[0][5], new String[]{"ID", "Student", "Document", "Status", "Requested"}); table.setFillsViewportHeight(true); body.add(new JScrollPane(table), BorderLayout.CENTER);
            JButton update = button("Accept / Update Status"); body.add(update, BorderLayout.SOUTH); update.addActionListener(e -> edit()); refresh(); setLayout(new BorderLayout()); add(root);
        }
        void refresh() {
            visible.clear(); for (Request r : DATA.requests) if (r.department.equals(current.department)) visible.add(r);
            Object[][] rows = new Object[visible.size()][5]; for (int i = 0; i < visible.size(); i++) { Request r = visible.get(i); rows[i] = new Object[]{r.id.substring(0, 8), r.studentId, r.document, r.status, r.created}; }
            table.setModel(new javax.swing.table.DefaultTableModel(rows, new String[]{"ID", "Student", "Document", "Status", "Requested"}));
        }
        void edit() {
            int row = table.getSelectedRow(); if (row < 0) { message("Select a request."); return; } Request r = visible.get(table.convertRowIndexToModel(row));
            JComboBox<String> status = new JComboBox<>(STATUSES); status.setSelectedItem(r.status); JTextField month = new JTextField(r.pickupMonth, 12), date = new JTextField(r.pickupDate, 12), time = new JTextField(r.pickupTime, 12);
            JPanel p = new JPanel(new GridLayout(0, 2, 6, 6)); p.add(new JLabel("Document status")); p.add(status); p.add(new JLabel("Pickup month")); p.add(month); p.add(new JLabel("Pickup date")); p.add(date); p.add(new JLabel("Pickup time")); p.add(time);
            if (JOptionPane.showConfirmDialog(frame, p, "Update Status", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            String selected = (String) status.getSelectedItem(); if ("Ready for Pickup".equals(selected) && (month.getText().trim().isEmpty() || date.getText().trim().isEmpty() || time.getText().trim().isEmpty())) { message("Pickup month, date, and time are required for Ready for Pickup."); return; }
            r.status = selected; r.pickupMonth = month.getText().trim(); r.pickupDate = date.getText().trim(); r.pickupTime = time.getText().trim(); DATA.save(); refresh();
        }
    }

    static final class AboutView extends JPanel {
        AboutView() {
            JPanel root = page("About Us"), body = bodyOf(root); JTextArea text = new JTextArea(
                """
                HISTORICAL SKETCH
                
                The City of Malabon University is a local university owned and operated by the local government unit of Malabon. Its primary purpose is to help promote the interest and welfare of its people especially the youth by providing them with meaningful opportunities geared towards the demands of an emerging global village. It was known first as the Malabon Community College in 1993 through Ordinance No. 14-1993 of the Municipal Council of Malabon.
                
                It was temporarily located at Malabon High School, Panghulo Annex. The classes all started on June 1994 when it initially offered Bachelor of Science in Education, Bachelor of Elementary Education, and Two-year Secretarial Courses. The first president was DR. PABLO S. TANGCO and the late MAYOR PROSPERO I. ORETA was the Ex-Officio Chairperson of the Board of Trustees.
                
                In 1996, it was transferred to a more spacious lot at Pampano St. Corner Maya-maya St. Barangay Longos, Malabon City.
                
                Bachelor of Science in Business Administration and Bachelor of Science in Accountancy courses were offered four years later after the school was founded.
                
                In 1997, Graduate Studies and Applied Research (GSAR) was offered for the first time. It catered to 43 MA students who used to be teachers from Malabon and Navotas. Soon MAED was followed by MAEM, MBA, MPA, and Doctoral courses.
                
                Next Chairperson of the Board was MAYOR AMADO VICENCIO. He led the City Council of Malabon on March 14, 2002, in amending City Ordinance No.14 S 1993 to enact the Malabon City College Charter, through the passage of City Ordinance No. 05,2002.
                
                Malabon had been a highly urbanized City. By virtue of a congressional act sponsored by HON. CONGRESSMAN FEDERICO S. SANDOVAL, the name Malabon City College was renamed as the City of Malabon University or CMU through an Ordinance No. 2003, approved by the City Council of Malabon on June 3, 2003.
                
                The first university president was DR. CORAZON VICARIO who was succeeded by DR. ASUNCION R. HOWE until ATTY. RAMON MARONILLA took over with CITY MAYOR CANUTO ORETA and was Ex-Officio Chair of the Board of Regents. By then MAYOR ANTOLIN A. ORETA III took over as the university president himself.
                
                The additional offering of courses did not stop from there, because lately the College of Arts and Sciences, the College of Engineering and Technology, and the College of Criminology were opened.
                
                Now, in the year 2022, CMU is still in its transition period, the Chair of the Board of Regents is MAYOR JEANNIE N. SANDOVAL with DR. GLEN DV DE LEON as OIC, President.
                
                After almost 28 years, the City of Malabon University (CMU) has grown to offerings of different programs. With the new set of statements of its vision, mission, and objectives, it is hoped that changes would occur in the process of finding where to go and how to go there.
                
                UNIVERSITY SEAL
                
                The Shield: A symbol for solidarity and honor, this design element rendered in an emblematic shape of a notched shield, encloses the other elements within bold blue lines that connote emphasis and significance.
                
                The Torch: The lighted torch symbolizes the burning passion one must have for acquiring knowledge and for honing skills.
                
                The Globe: The globe with grids represent the ambition for global standards for education and production of globally competitive students. Its orbiting illusion stands for its continuous desire to do so.
                
                The Book and Academic Cap: An icon assembled from a combined stencil of an academic cap and a book set to remind one to hold education and achievement in high regard.
                
                The School of Fish: The School of Fish represents one of Malabon City\u2019s main industry. It also symbolizes the roots of our students and the foundation of their existence.
                
                VISION
                A leading university that is committed for the advancement of human lives and global competitiveness through academic excellence, research and community linkages.
                
                MISSION
                CMU aims to provide transformative and globally competitive higher education, holistic formation, research and innovation, excellent delivery of services, and sustainable community extension and linkages.
                
                Core Values
                C \u2013 Competent
                M \u2013 Morally upright
                U \u2013 United
                A \u2013 Adaptable
                H \u2013 Honest
                O \u2013 Optimistic
                N \u2013 Nationalistic
                
                CONTACT DETAILS
                Our Address: Pampano cor., Maya-Maya Sts., Longos City of Malabon
                Our Mailbox: ro@cityofmalabonuniversity.edu.ph
                Our Phone: (02) 8288-0000""");
            text.setEditable(false); text.setLineWrap(true); text.setWrapStyleWord(true); text.setFont(new Font("SansSerif", Font.PLAIN, 16)); text.setBorder(new EmptyBorder(20, 25, 20, 25));
            body.add(new JScrollPane(text), BorderLayout.CENTER); setLayout(new BorderLayout()); add(root);
        }
    }
    static final class ContactView extends JPanel {
        ContactView() {
            JPanel root = page("Contact Us"), body = bodyOf(root); JPanel form = new JPanel(new GridBagLayout()); form.setBackground(Color.WHITE); form.setBorder(new EmptyBorder(20, 25, 20, 25)); GridBagConstraints gc = constraints();
            JTextField email = new JTextField(30); JTextArea message = new JTextArea(8, 30); message.setLineWrap(true); message.setWrapStyleWord(true);
            field(form, gc, "Email *", email); gc.gridy++; field(form, gc, "Message *", new JScrollPane(message)); gc.gridy++; JButton send = button("Send Message"); form.add(send, gc); body.add(form, BorderLayout.NORTH);
            send.addActionListener(e -> { String em = email.getText().trim(); if (em.isEmpty() || message.getText().trim().isEmpty()) { Main.message("Email and message are required."); return; }
                if (!em.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) { Main.message("Enter a valid email address."); return; } Main.message("Thank you. Your message has been sent."); email.setText(""); message.setText(""); });
            setLayout(new BorderLayout()); add(root);
        }
    }

    static final class User implements Serializable {
        private static final long serialVersionUID = 1L; String id, password, department; boolean admin;
        User(String id, String password, String department, boolean admin) { this.id = id; this.password = password; this.department = department; this.admin = admin; }
    }
    static final class Request implements Serializable {
        private static final long serialVersionUID = 1L; String id, studentId, name, email, course, contact, department, document, status = "Pending", created, pickupMonth = "", pickupDate = "", pickupTime = "";
        Request(String id, String studentId, String name, String email, String course, String contact, String department, String document) {
            this.id = id; this.studentId = studentId; this.name = name; this.email = email; this.course = course; this.contact = contact; this.department = department; this.document = document; created = LocalDateTime.now().format(DATE);
        }
        String pickup() { return pickupMonth.isEmpty() ? "" : pickupMonth + " / " + pickupDate + " / " + pickupTime; }
    }
    static final class Data implements Serializable {
        private static final long serialVersionUID = 1L; Map<String, User> students = new LinkedHashMap<>(), admins = new LinkedHashMap<>(); java.util.List<Request> requests = new ArrayList<>();
        void save() { try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(STORE))) { out.writeObject(this); } catch (IOException ex) { message("Unable to save data: " + ex.getMessage()); } }
        static Data load() { if (!STORE.exists()) return new Data(); try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(STORE))) { return (Data) in.readObject(); } catch (Exception ex) { return new Data(); } }
    }
}
