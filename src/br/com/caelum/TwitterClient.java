package br.com.caelum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.util.Log;

import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.Tweet;

/**
 * Busca tweets de acordo com um filtro
 * 
 * @author Jonas Alves
 */
public class TwitterClient {
	/**
	 * Guarga o id do último tweet devolvido pelo twitter
	 */
	private String ultimoId = "0";

	/**
	 * Filtro que será usado na busca
	 */
	final String filtro;

	public TwitterClient(String filtro) {
		this.filtro = filtro;
	}

	/**
	 * Recupera os tweets que foram postados desde a última vez que esse método
	 * foi chamado. Se esse método nunca foi chamado, retorna todos os tweets.
	 * 
	 * @return List<Tweet> os últimos tweets
	 */
	public List<Tweet> getTweets() {
		SearchDevice search = SearchDevice.getInstance();

		Query query = montaQuery();

		List<Tweet> novosTweets = new ArrayList<Tweet>();
		try {
			novosTweets.addAll(Arrays.asList(search.searchTweets(query)));
		} catch (Exception e) {
			Log.i("DroidInSampa", "Problemas na chamada da API do Twitter");
			e.printStackTrace();
		}

		if (novosTweets.size() > 0)
			ultimoId = novosTweets.get(0).getString("TWEET_ID");

		return novosTweets;
	}

	/**
	 * Monta a query de busca de tweets contendo o filtro, a quantidade máxima e
	 * o id do último tweet que já foi recuperado
	 * 
	 * @return Query: a query de busca de tweets
	 */
	private Query montaQuery() {
		// que contenham o texto do filtro
		Query query = QueryComposer.containAll(filtro);
		// no máximo 50 tweets
		query = QueryComposer.append(query, QueryComposer.resultCount(50));
		// que foram criados depois da última verificação
		query = QueryComposer.append(query, QueryComposer.sinceID(ultimoId));

		return query;
	}
}
