package br.com.caelum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.Tweet;

/**
 * Implementação completa do DroidInSampa com alguns recursos a mais que na
 * palestra. Essa versão executa a cada 2 min a busca e atualiza a ListView com
 * os dados mais recentes
 * 
 * Todo o código está bem documentado e quebrado em pequenos métodos com nomes
 * grandes e legíveis para facilitar a compreensão.
 * 
 * @author Sérgio Lopes - @sergio_caelum
 * @see http://www.caelum.com.br
 */
public class DroidInSampa extends ListActivity {

	// lista de tweets a serem exibidos; é uma lista de objetos do tipo linha
	private List<Tweet> tweets = new ArrayList<Tweet>();

	// timer para executar as chamadas com frequencia
	private Timer timer = new Timer();

	// auxiliar: guarda o ultimoId devolvido pelo Twitter (inicialmente zero)
	private String ultimoId = "0";

	/**
	 * Método do Android que é chamado quando a aplicação inicia
	 */
	public @Override
	void onCreate(Bundle savedInstanceState) {
		inicializacaoDaAplicacaoEVariaveis(savedInstanceState);
		disparaTaskBackground();
	}

	private void inicializacaoDaAplicacaoEVariaveis(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	private void disparaTaskBackground() {
		TimerTask background = new TimerTask() {
			public void run() {
				final List<Tweet> novosTweets = chamaTwitter();

				// (importante, avançado)
				// Dispara a atualização da tela (ListView) na Thread de UI do
				// Android.
				// Não podemos mexer na tela em uma thread diferente da
				// UI-thread
				runOnUiThread(new Runnable() {
					public void run() {
						populaList(novosTweets);
						notificacoes(novosTweets);
					}
				});
			}
		};
		timer.schedule(background, 0, 1000 * 60 * 2);
	}

	private List<Tweet> chamaTwitter() {
		SearchDevice search = SearchDevice.getInstance();

		// busca por todos os termos, ...
		Query query = QueryComposer.containAll("@sergio_caelum #devinsampa");

		// ... com no máximo 50 resultados, ...
		query = QueryComposer.append(query, QueryComposer.resultCount(50));

		// ... e só os que chegaram desde a ultima checagem
		query = QueryComposer.append(query, QueryComposer.sinceID(ultimoId));

		List<Tweet> novosTweets = new ArrayList<Tweet>();
		try {
			novosTweets.addAll(Arrays.asList(search.searchTweets(query)));
			if (novosTweets.size() > 0)
				ultimoId = novosTweets.get(0).getString("TWEET_ID");
		} catch (Exception e) {
			Log.i("DroidInSampa", "Problemas na chamada da API do Twitter");
			e.printStackTrace();
		}
		return novosTweets;
	}

	// deve ser chamado na UI-Thread
	private void populaList(final List<Tweet> novosTweets) {
		// adiciona novos tweets no início da lista pre-existente
		tweets.addAll(0, novosTweets);

		// atualiza a lista com os novos tweets
		setListAdapter(new TweetArrayAdapter(this, R.layout.linha, R.id.texto,
				tweets));
	}

	private void notificacoes(List<Tweet> tweets) {
		if (tweets.size() > 4) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.maisdecinco);
			mp.start();
		} else if (tweets.size() > 0) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.tweetnovo);
			mp.start();
		}

		// mostra um Toast (popup) com o número de novos tweets
		Toast.makeText(this, tweets.size() + " novos tweets",
				Toast.LENGTH_SHORT).show();
	}

	// quando usuário sair da tela, desliga o programa
	protected @Override
	void onPause() {
		super.onPause();
		System.exit(0);
		// TODO: Remover esse System.exit(). Não esquecer de parar o timer
	}
}
