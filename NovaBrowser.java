
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class NovaBrowser {

    private static CefApp cefApp;
    private static CefClient cefClient;
    private static boolean ready = false;
    private static boolean loading = false;

    public static void open() {

        if (ready) {
            showBrowser();
            return;
        }

        if (loading) {
            JOptionPane.showMessageDialog(
                    null,
                    "Nova Browser todavía se está preparando."
            );
            return;
        }

        loading = true;

        JFrame waitFrame = new JFrame("Nova Browser");
        waitFrame.setSize(450, 130);
        waitFrame.setLocationRelativeTo(null);

        JLabel message = new JLabel(
                "Preparando Chromium. Puede tardar unos minutos...",
                SwingConstants.CENTER
        );

        waitFrame.add(message);
        waitFrame.setVisible(true);

        SwingWorker<CefApp, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected CefApp doInBackground() throws Exception {

                        CefAppBuilder builder = new CefAppBuilder();

                        builder.setInstallDir(
                                new File("jcef-bundle")
                        );

                        builder.addJcefArgs(
                                "--disable-gpu",
                                "--no-sandbox"
                        );

                        builder.getCefSettings()
                                .windowless_rendering_enabled = false;

                        return builder.build();
                    }

                    @Override
                    protected void done() {

                        waitFrame.dispose();
                        loading = false;

                        try {
                            cefApp = get();
                            cefClient = cefApp.createClient();
                            ready = true;

                            showBrowser();

                        } catch (Exception ex) {
                            ex.printStackTrace();

                            JOptionPane.showMessageDialog(
                                    null,
                                    "No se pudo iniciar Chromium.\n"
                                            + "Comprueba tu conexión y la "
                                            + "configuración de Maven.\n\n"
                                            + ex.getMessage(),
                                    "Error de Nova Browser",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                };

        worker.execute();
    }

    private static void showBrowser() {

        JFrame frame = new JFrame("Nova Browser 2.0");
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(750, 500));
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JTextField address = new JTextField(
                "https://www.google.com"
        );

        address.setFont(new Font("Arial", Font.PLAIN, 15));

        JButton back = new JButton("⬅");
        JButton forward = new JButton("➡");
        JButton reload = new JButton("⟳");
        JButton go = new JButton("Ir");

        JPanel navigation = new JPanel(new BorderLayout(5, 5));
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT));

        controls.add(back);
        controls.add(forward);
        controls.add(reload);

        navigation.add(controls, BorderLayout.WEST);
        navigation.add(address, BorderLayout.CENTER);
        navigation.add(go, BorderLayout.EAST);

        CefBrowser browser = cefClient.createBrowser(
                "https://www.google.com",
                false,
                false
        );

        Runnable navigate = () -> {
            String url = address.getText().trim();

            if (url.isEmpty()) return;

            if (!url.startsWith("https://")
                    && !url.startsWith("http://")) {

                if (url.contains(" ")) {
                    url = "https://www.google.com/search?q="
                            + url.replace(" ", "+");
                } else {
                    url = "https://" + url;
                }
            }

            browser.loadURL(url);
        };

        go.addActionListener(e -> navigate.run());
        address.addActionListener(e -> navigate.run());

        back.addActionListener(e -> browser.goBack());
        forward.addActionListener(e -> browser.goForward());
        reload.addActionListener(e -> browser.reload());

        frame.add(navigation, BorderLayout.NORTH);
        frame.add(browser.getUIComponent(), BorderLayout.CENTER);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(
                    java.awt.event.WindowEvent e) {
                browser.close(true);
            }
        });

        frame.setVisible(true);
    }
}
