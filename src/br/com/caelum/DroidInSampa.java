package br.com.caelum;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.twitterapime.search.Query;
import com.twitterapime.search.QueryComposer;
import com.twitterapime.search.SearchDevice;
import com.twitterapime.search.Tweet;

/**
 * Implementação completa do DroidInSampa com alguns recursos a mais que na palestra.
 * Essa versão executa a cada 2 min a busca e atualiza a ListView com os dados mais recentes
 * 
 * Todo o código está bem documentado e quebrado em pequenos métodos com nomes grandes
 * e legíveis para facilitar a compreensão.
 * 
 * @author Sérgio Lopes - @sergio_caelum
 * @see http://www.caelum.com.br
 */
public class DroidInSampa extends Activity {
	
	// representa a lista colocada no main.xml
	private ListView listaDeTweets;
	
	// lista de tweets a serem exibidos; é uma lista de objetos do tipo linha
    private Vector<View> linhas = new Vector<View>();

    // timer para executar as chamadas com frequencia
    private Timer timer = new Timer();
    
    // auxiliar: guarda o ultimoId devolvido pelo Twitter (inicialmente zero)
    private String ultimoId = "0";
    
	/**
	 * Método do Android que é chamado quando a aplicação inicia
	 */
    public @Override void onCreate(Bundle savedInstanceState) {
        inicializacaoDaAplicacaoEVariaveis(savedInstanceState);
        disparaTaskBackground();
	}
    
    private void inicializacaoDaAplicacaoEVariaveis(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.main);
	    listaDeTweets = (ListView) findViewById(R.id.tweets);
	}

	private void disparaTaskBackground() {
		TimerTask background = new TimerTask() {
			public void run() {
				final Tweet[] tweets = chamaTwitter();
				final List<View> linhas = montaLinhasNovas(tweets);
				
				// (importante, avançado)
				// Dispara a atualização da tela (ListView) na Thread de UI do Android.
				// Não podemos mexer na tela em uma thread diferente da UI-thread
				runOnUiThread(new Runnable() {
					public void run() {
						populaList(linhas);
						notificacoes(tweets);
					}
				});
			}
		};
		timer.schedule(background, 0, 1000 * 60 * 2);
	}

	private Tweet[] chamaTwitter() {
		SearchDevice search = SearchDevice.getInstance();
		
		// busca por todos os termos, ...
		Query query = QueryComposer.containAll("@sergio_caelum #devinsampa");
		
		// ... com no máximo 50 resultados, ...
		query = QueryComposer.append(query, QueryComposer.resultCount(50));
		
		// ... e só os que chegaram desde a ultima checagem
		query = QueryComposer.append(query, QueryComposer.sinceID(ultimoId));
	
		try {
			Tweet[] tweets = search.searchTweets(query);
			if (tweets.length > 0)
				ultimoId = tweets[0].getString("TWEET_ID");
			return tweets;
		} catch (Exception e) {
			Log.i("DroidInSampa", "Problemas na chamada da API do Twitter");
			e.printStackTrace();
			return new Tweet[0];
		}
	}

	private List<View> montaLinhasNovas(Tweet[] tweets) {
		LayoutInflater vi = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		List<View> linhas = new ArrayList<View>();
		
		for (Tweet tweet : tweets) {
		    View view = vi.inflate(R.layout.linha, null);
		    montaFotoDoTweet(tweet, view);
		    montaTextoDoTweet(tweet, view);
		    linhas.add(view);
		}
		return linhas;
	}

	private void montaTextoDoTweet(Tweet tweet, View linha) {
		String conteudo = tweet.getString("TWEET_CONTENT");
		String usuario  = tweet.getString("TWEET_AUTHOR_USERNAME");
		
		TextView texto = (TextView) linha.findViewById(R.id.texto);
		texto.setText(usuario + ": " + conteudo);
	}

	private void montaFotoDoTweet(final Tweet tweet, View linha) {
		String fotoURI = tweet.getString("TWEET_AUTHOR_PICTURE_URI");
		ImageView foto = (ImageView) linha.findViewById(R.id.foto);
	
		try {
			URL url = new URL(fotoURI); 
			InputStream stream = url.openStream();
			Bitmap bmp = BitmapFactory.decodeStream(stream);
			stream.close();
			foto.setImageBitmap(bmp);
			
			// ao clicar na foto, abre o profile do usuário no browser
			foto.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					abreBrowser(tweet.getString("TWEET_AUTHOR_URI"));
				}
			});
		} catch (IOException e) {
			Log.i("DroidInSampa", "Problemas ao baixar a foto do usuário, ignorando ele");
			e.printStackTrace();
		}
	}

	// deve ser chamado na UI-Thread
	private void populaList(final List<View> novasLinhas) {
		// adiciona novas linhas no início da lista pre-existente
		linhas.addAll(0, novasLinhas);
		
		// atualiza a lista com os novos tweets
		listaDeTweets.setAdapter(new ArrayAdapter<View>(this, android.R.layout.simple_list_item_1, linhas) {
			public View getView(int position, View convertView, ViewGroup parent) {
				return linhas.get(position);
			}
		});
	}

	private void notificacoes(Tweet[] twts) {
		if (twts.length > 4) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.maisdecinco);
			mp.start();
		} else if (twts.length > 0) {
			MediaPlayer mp = MediaPlayer.create(this, R.raw.tweetnovo);
			mp.start();
		}
		
		// mostra um Toast (popup) com o número de novos tweets
		Toast.makeText(this, twts.length + " novos tweets", Toast.LENGTH_SHORT).show();
	}
	
	private void abreBrowser(String url) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(url));
		startActivity(i);
	}

	// quando usuário sair da tela, desliga o programa
	protected @Override void onPause() {
		super.onPause();
		System.exit(0);
	}
}

