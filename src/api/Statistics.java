package api;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import zookeeper.Configuration;

/**
 * Servlet implementation class Statistics
 */
@WebServlet("/Statistics")
public class Statistics extends HttpServlet {
	private static final long serialVersionUID = 1L;

    private static Statistics StatsInstance = null;
    public static Statistics getInstance() {
		if (StatsInstance == null) {
			StatsInstance = new Statistics();
		}
		return StatsInstance;
	}

       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Statistics() {
        super();
        
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("UTF-8");
		int now=(int)(System.currentTimeMillis()/1000);
		int timeLapsed=(int)((now-Configuration.getTimeStarted())/60);
		response.getWriter().println("Total Reads= "+Configuration.getStats().getRreads()+" Total writes= "+Configuration.getStats().getWrites());
		
		response.getWriter().println("Reads per min= "+Configuration.getStats().getRreads()/timeLapsed+" Writes per min= "+Configuration.getStats().getWrites()/timeLapsed);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
