package br.com.caelum;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.twitterapime.search.Tweet;

public class TweetArrayAdapter extends ArrayAdapter<Tweet> {

	public TweetArrayAdapter(Context context, int resource,
			int textViewResourceId) {
		super(context, resource, textViewResourceId);
		// TODO Auto-generated constructor stub
	}

	/**
	 * A ViewHolder vai guardar a referência para as views mais acessadas, para
	 * que usemos o View.findViewById apenas uma vez, pois ele é <b>muito</b>
	 * custoso.
	 * 
	 * @author Jonas Alves
	 */
	private static class ViewHolder {
		TextView texto;
		ImageView foto;
	}

	/**
	 * Cria uma nova view que representa um Tweet na lista.<br />
	 * Caso esteja disponível, aproveitamos a <i>convertView</i>, pois criar uma
	 * nova é sempre mais custoso.
	 * 
	 * @return View: A view que representará o tweet na lista.
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null)
			convertView = inflateWithHolder();
		ViewHolder holder = (ViewHolder) convertView.getTag();

		final Tweet tweet = this.getItem(position);
		holder.texto.setText(montarTexto(tweet));
		holder.foto.setImageBitmap(montarFoto(tweet));
		abrirBrowserAoClicar(holder.foto, tweet.getString("TWEET_AUTHOR_URI"));

		return convertView;
	}

	/**
	 * Ao clicar na foto, abre o profile do usuário no browser
	 * 
	 * @param foto
	 * @param tweet
	 */
	private void abrirBrowserAoClicar(ImageView foto, final String uri) {
		foto.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// Abre o browser
				Intent i = new Intent(Intent.ACTION_VIEW);
				i.setData(Uri.parse(uri));
				getContext().startActivity(i);
			}
		});
	}

	private String montarTexto(Tweet tweet) {
		return tweet.getString("TWEET_CONTENT") + ": "
				+ tweet.getString("TWEET_AUTHOR_USERNAME");
	}

	private Bitmap montarFoto(final Tweet tweet) {
		String fotoURI = tweet.getString("TWEET_AUTHOR_PICTURE_URI");

		InputStream stream = null;
		Bitmap bmp = null;
		try {
			URL url = new URL(fotoURI);
			stream = url.openStream();
			bmp = BitmapFactory.decodeStream(stream);
		} catch (IOException e) {
			Log.i("DroidInSampa",
					"Problemas ao baixar a foto do usuário, ignorando ele");
			e.printStackTrace();
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
			}
		}
		return bmp;
	}

	/**
	 * Infla uma nova view <i>R.layout.linha</i> e seu ViewHolder.
	 * 
	 * @return View: a nova view com ViewHolder
	 */
	private View inflateWithHolder() {
		View view = View.inflate(getContext(), R.layout.linha, null);
		ViewHolder holder = new ViewHolder();
		holder.texto = (TextView) view.findViewById(R.id.texto);
		holder.foto = (ImageView) view.findViewById(R.id.foto);
		view.setTag(holder);
		return view;
	}

}
