package live.nettools.util;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Classe responsÃ¡vel por oferecer serviÃ§os para os valores de string
 * 
 * @author Evandro
 */
public class UtilCollection<OBJ> implements Serializable{

	private static final long serialVersionUID = -409967793242588012L;

	/**
	 * Método que ordena a coleção
	 * 
	 * @param lista
	 *            List
	 * @param campo
	 *            String
	 */
	@SuppressWarnings("unchecked")
	public void ordenarListaDesc(List<OBJ> lista, String campo) {
		Collections.sort(lista, new ComparatorList(campo));
		Collections.reverse(lista);
	}

	/**
	 * Método que ordena a coleção
	 * 
	 * @param lista
	 *            List
	 * @param campo
	 *            String
	 */
	@SuppressWarnings("unchecked")
	public void ordenarListaAsc(List lista, String campo) {
		Collections.sort(lista, new ComparatorList(campo));
	}

	/**
	 * Converte um Set em uma Lista. Caso o Set informado seja null sera lancada
	 * uma excecao {@link NullPointerException}
	 * 
	 * @param <T>
	 *            Tipo do Conteudo da lista
	 * @param set
	 *            Set a ser convertido em Lista
	 * @return List<T> Lista gerada a partir do Set
	 */
	public <T> List<T> convertSetToList(Set<T> set) {
		return new ArrayList<T>(set);
	}

	/**
	 * Converte um Set em uma Lista ordenando a Lista pela propriedade
	 * informada. Caso o Set informado seja null sera lancada uma excecao
	 * {@link NullPointerException}
	 * 
	 * @param <T>
	 *            Tipo do Conteudo da lista
	 * @param set
	 *            Set a ser convertido em Lista
	 * 
	 * @param propriedadeOrdenacao
	 *            Propriedade dos conteudos da Lista que indicarao a ordenacao
	 * @return List<T> Lista gerada a partir do Set
	 */
	public <T> List<T> convertSetToList(Set<T> set,
			String propriedadeOrdenacao) {
		 UtilString utilString = new UtilString();
		if (utilString.vazio(propriedadeOrdenacao)) {
			throw new IllegalArgumentException(
					"Propriedade de ordenacao Invalida");
		}
		final List<T> converted = convertSetToList(set);
		Object content = converted.get(0);
		Field[] fields = content.getClass().getDeclaredFields();
		Field propertyToSet = null;
		for (Field field : fields) {
			if (propriedadeOrdenacao.equals(field.getName())) {
				propertyToSet = field;
				break;
			}
		}
		if (propertyToSet == null) {
			throw new IllegalArgumentException(
					"Propriedade nao encontrada no tipo ".concat(content
							.getClass().getName()));
		}
		ordenarListaAsc(converted, propriedadeOrdenacao);
		return converted;

	}
}