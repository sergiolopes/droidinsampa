package br.com.caelum;

import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ListActivity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Toast;

import com.twitterapime.search.Tweet;

/**
 * Implementação completa do DroidInSampa com alguns recursos a mais que na
 * palestra. Essa versão executa a busca a cada 1 min e atualiza o ArrayAdapter
 * da ListView com os dados mais recentes
 * 
 * Todo o código está bem documentado e quebrado em pequenos métodos com nomes
 * grandes e legíveis para facilitar a compreensão.
 * 
 * @author Sérgio Lopes - @sergio_caelum
 * @see http://www.caelum.com.br
 */
public class DroidInSampa extends ListActivity {

	// timer para buscar novos tweets periódicamente
	private Timer timer = new Timer();

	// Classe que acessa o twitter
	private TwitterClient twitter = new TwitterClient(
			"@sergio_caelum #devinsampa");

	// Método do Android que é chamado quando a aplicação inicia
	@Override
	public void onCreate(Bundle savedInstanceState) {
		inicializacaoDaAplicacaoELayout(savedInstanceState);
		disparaTaskBackground();
	}

	private void inicializacaoDaAplicacaoELayout(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
	}

	/**
	 * Inicia o <i>Timer</i> para verificar novos tweets periódicamente
	 */
	private void disparaTaskBackground() {
		TimerTask background = new TimerTask() {
			public void run() {
				final List<Tweet> novosTweets = twitter.getTweets();

				// (importante, avançado)
				// Dispara a atualização da tela (ListView) na Thread de UI do
				// Android. Não podemos mexer na tela em uma thread diferente da
				// UI-thread
				runOnUiThread(new Runnable() {
					public void run() {
						populaList(novosTweets);
						tocaSom(novosTweets);
						mostraQuantidadeNaTela(novosTweets);
					}
				});
			}
		};
		timer.schedule(background, 0, 1000 * 60 * 1);
	}

	/**
	 * Adiciona na lista da tela os tweets Deve ser chamado na UI-Thread
	 */
	private void populaList(final List<Tweet> novosTweets) {
		TweetArrayAdapter adapter = getAdapter();

		// Inverte a ordem da lista de novos tweets para que os mais recentes
		// fiquem sempre acima
		Collections.reverse(novosTweets);

		// adiciona novos tweets no início da lista pre-existente
		for (Tweet tweet : novosTweets) {
			adapter.insert(tweet, 0);
		}

		// atualiza a lista com os novos tweets
		setListAdapter(adapter);
	}

	/**
	 * Recupera o ListAdapter relacionado à Activity. Se este não existir, um
	 * novo é criado.
	 * 
	 * @return TweetArrayAdapter
	 */
	private TweetArrayAdapter getAdapter() {
		TweetArrayAdapter adapter = (TweetArrayAdapter) getListAdapter();
		if (adapter == null)
			adapter = new TweetArrayAdapter(this, R.layout.linha, R.id.texto);

		return adapter;
	}

	/**
	 * Toca um som para avisar que novos tweets foram recebidos
	 * 
	 * @param tweets
	 */
	private void tocaSom(List<Tweet> tweets) {
		if (tweets.size() > 4) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.maisdecinco);
			mp.start();
		} else if (tweets.size() > 0) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.tweetnovo);
			mp.start();
		}
	}

	/**
	 * Mostra um Toast (popup) com o número de novos tweets
	 */
	private void mostraQuantidadeNaTela(List<Tweet> tweets) {
		Toast.makeText(this, tweets.size() + " novos tweets",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Quando usuário sair da tela, finaliza o <i>Timer</i> e o programa
	 */
	@Override
	protected void onPause() {
		super.onPause();
		timer.cancel();
		finish();
	}
}
