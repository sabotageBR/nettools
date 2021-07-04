package live.nettools.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet({"/facebook/login"})
public class FacebookLogin extends HttpServlet {

	private static final long serialVersionUID = 4043679811548773728L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		File arquivo = null;
		try {
			String nomeArquivo = req.getParameter("file");
			arquivo = new File(nomeArquivo);
			if(arquivo.exists()) {
	            resp.setContentType("application/force-download");
	            resp.setHeader("Content-Disposition","attachment; filename=\"" +nomeArquivo+ "\"");
	            int BUFF_SIZE = 1024;
	            byte[] buffer = new byte[BUFF_SIZE];
	            resp.setContentLength((int) arquivo.length());
	            FileInputStream fis = new FileInputStream(arquivo);
	            OutputStream os = resp.getOutputStream();
	            int byteCount = 0;
	
	            do {
	                byteCount = fis.read(buffer);
	                if (byteCount == -1) {
	                    break;
	                }
	                os.write(buffer, 0, byteCount);
	                os.flush();
	            } while (true);
			}else {
				System.out.println("Arquivo nao existe: "+arquivo.getAbsolutePath());
			}
        }catch(Exception e) {
        	e.printStackTrace();
        } finally {
        	if(arquivo !=null && arquivo.exists()) {
        	//	arquivo.delete();
        	}
        }
	}
	
}
