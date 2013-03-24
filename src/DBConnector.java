import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JTextArea;

import com.mysql.jdbc.ResultSetMetaData;

import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class DBConnector {

	private JFrame frame;
	private String username, password;
	private JButton btnConnect, btnDisconnect, btnExecute, btnExit, btnReset, btnClear;
	private JTextArea textArea;
	private MysqlHandler conn;
	private JTextArea textAreaQueryResult;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DBConnector window = new DBConnector();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public DBConnector() {
		initialize();
        openConnection();		
	}

	/**
	 * Initialize the contents of the frame.
	 * @wbp.parser.entryPoint
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 669, 372);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openConnection();
			}
		});
		btnConnect.setBounds(12, 28, 117, 25);
		frame.getContentPane().add(btnConnect);
		
		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

					closeConnection(); 

			}
		});
		btnDisconnect.setBounds(12, 65, 117, 25);
		frame.getContentPane().add(btnDisconnect);
		
		btnExecute = new JButton("Execute");
		btnExecute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				executeQuery(textArea.getText());
			}
		});
		btnExecute.setBounds(67, 164, 117, 25);
		frame.getContentPane().add(btnExecute);
		
		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(new BorderLayout());
		queryPanel.add(scrollPane,BorderLayout.CENTER);
		queryPanel.setBounds(154, 12, 490, 136);
		frame.getContentPane().add(queryPanel);
		
		btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				frame.dispose();
			}
		});
		btnExit.setBounds(12, 102, 117, 25);
		frame.getContentPane().add(btnExit);
		
		btnReset = new JButton("Reset Query");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    textArea.setText(null);
			}
		});
		btnReset.setBounds(258, 164, 129, 25);
		frame.getContentPane().add(btnReset);
		
		btnClear = new JButton("Clear Results");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textAreaQueryResult.setText(null);
			}
		});
		btnClear.setBounds(486, 164, 129, 25);
		frame.getContentPane().add(btnClear);
		
		textAreaQueryResult = new JTextArea();
		textAreaQueryResult.setLineWrap(true);
		textAreaQueryResult.setWrapStyleWord(true);
		JScrollPane scrollPaneResult = new JScrollPane(textAreaQueryResult);
		JPanel queryPanelResult = new JPanel();
		queryPanelResult.setLayout(new BorderLayout());
		queryPanelResult.add(scrollPaneResult,BorderLayout.CENTER);
		queryPanelResult.setBounds(12, 205, 632, 118);
		frame.getContentPane().add(queryPanelResult);
		
	}

	
	private void promptForCredentials() {
	
		username = JOptionPane.showInputDialog("Database Username:");
		password = JOptionPane.showInputDialog("Database Password:");
		
	}
	
	private boolean authenticate() {
	
		conn = new MysqlHandler(username,password);
		JOptionPane.showMessageDialog(null,conn.getAuthenticationResults(),"Authentication Result",JOptionPane.PLAIN_MESSAGE);
		return conn.isAuthenticated();
	}
	
	private void openConnection() {
		
		promptForCredentials();
		if(authenticate()) {
			btnConnect.setEnabled(false);
		    btnDisconnect.setEnabled(true);
		}
		else {
			btnDisconnect.setEnabled(false);
		    btnConnect.setEnabled(true);
		}
	}
	
	private void closeConnection() {

		try {
			conn.getConnection().close();

			if(conn.getConnection().isClosed()) {
				btnConnect.setEnabled(true);
				btnDisconnect.setEnabled(false);
				JOptionPane.showMessageDialog(null,"Successfully closed the connection","Closing Connection",JOptionPane.PLAIN_MESSAGE);
		    }
	    	else {
			    JOptionPane.showMessageDialog(null,"Cannot close the connection","Closing Connection",JOptionPane.ERROR_MESSAGE);	
		    }
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,"Cannot close the connection","Closing Connection",JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void executeQuery(String query) {
		
		String clause = getQueryClause(query);
		
		if(clause.equalsIgnoreCase("select")) {
	    	executeSelectQuery(query);
		}
	    else if(clause.equalsIgnoreCase("update") || clause.equalsIgnoreCase("insert") || clause.equalsIgnoreCase("delete")) {
	    	executeAlterQuery(query, clause);
	    }
	    else {
	    	textAreaQueryResult.append(String.format("'%s' clause is not supported \n",clause));
	    }
		
		//output errors if there are any
		if(!conn.getLastDbError().isEmpty()) { 
		    textAreaQueryResult.append(String.format("%s \n",conn.getLastDbError()));
		    conn.flushDbError();
		}
	}
	
	private void executeSelectQuery(String query) {
		
		ResultSetMetaData meta;
		int columnCount = 0;
		ResultSet result = conn.fetchQuery(query);
		try {
			meta = (ResultSetMetaData) result.getMetaData();
            columnCount = meta.getColumnCount();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	    try {
			while(result.next()) {
				
				for (int column = 1; column <= columnCount; column++)  {
					
				    textAreaQueryResult.append(result.getString(column) + " "); 
				}
				textAreaQueryResult.append("\r\n"); 
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
	}
	
	private void executeAlterQuery(String query, String clause) {
		
		System.out.println("not yet implemented");
		Integer affectedRows = conn.executeQuery(query);	
		textAreaQueryResult.append(String.format("performed '%s' on %d rows \n",clause, affectedRows));
	}
	
	public String getQueryClause(String query) {
		query = query.replaceAll("\\s+", " ");
		String arr[] = query.split(" ", 2);
		String clause = arr[0];	
        return clause;
	}
}
