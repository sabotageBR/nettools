package live.nettools.util;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 * Classe responsável por fornecer interface de comparação de listas.
 * 
 * @author Evandro
 */
@SuppressWarnings({"rawtypes" })
public class ComparatorList implements Comparator {

	private final String ordenarPor;
	
	/**
	 * Método Construtor da classe ComparatorList.
	 * @param ordenarPor String
	 */
	public ComparatorList(String ordenarPor) {
		super();
		this.ordenarPor = ordenarPor;
	}

	/**
	 * @param primeiro Object
	 * @param segundo Object
	 * @return int
	 */
	public int compare(Object primeiro, Object segundo) {
		int retorno = -1;
		try {
			if ((primeiro != null) && (segundo != null)) {				
				
				primeiro = this.invokeMethod(primeiro, this.ordenarPor);				
				segundo = this.invokeMethod(segundo, this.ordenarPor); 
				
				if ((primeiro instanceof String)) {
					retorno = primeiro.toString().compareToIgnoreCase(segundo.toString());
					
				} else if (this.isInstanciaNumerica(primeiro)) {
					BigDecimal numero = new BigDecimal(primeiro.toString());
					retorno = numero.compareTo(new BigDecimal(segundo.toString()));
				}
			}
		} catch (InvocationTargetException invoke) {
			invoke.printStackTrace();
		} catch (IllegalAccessException access) {
			access.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return retorno;
	}

	/**
	 * 
	 * @param primeiro
	 * @throws Exception 
	 * @throws IllegalArgumentException 
	 */
	private Object invokeMethod(Object obj, String chave) throws IllegalArgumentException, Exception {
		UtilReflection utilReflection = new UtilReflection();
		StringTokenizer stk = new StringTokenizer(chave, ".", false);
		while(stk.hasMoreTokens()) {
			obj = utilReflection.getMethod(obj, stk.nextToken()).invoke(obj, null);
		}
		
		return obj;
	}
	
	/**
	 * Método que verifica se um objeto é de instancia numerica.
	 * 
	 * @param object Object
	 * @return boolean
	 */
	private boolean isInstanciaNumerica(Object object) {
		boolean retorno = false;
		if (((object != null)
				&& (object instanceof Integer))
				|| (object instanceof Float)
				|| (object instanceof Double)
				|| (object instanceof Byte)
				|| (object instanceof Long)) {
			retorno = true;
		}
		return retorno;
	}
	
}