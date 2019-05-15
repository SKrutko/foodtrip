package com.example.foodtrip;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DetailedDishInfoActivity extends AppCompatActivity {

    private ArrayList<Dish> dishes;
    private Dish currentDish;

    ListView lvComments;
    CommentAdapter commentAdapter;
    ArrayList<Comment> commentsForCurrentCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_dish_info);

        dishes = DishCollection.getDishes();

        String choosenDish = getIntent().getStringExtra("ChooseDishResult");

        TextView tvDishName = findViewById(R.id.tvDishName);
        ImageView ivPicture = (ImageView) findViewById(R.id.ivPicture);
        final RatingBar rbRating = findViewById(R.id.rbRating);
        TextView tvOther = findViewById(R.id.tvOther);
        TextView tvBiggerDescription = findViewById(R.id.tvBiggerDescription);
        TextView tvNumberOfComments = findViewById(R.id.tvNumberOfComments);

        currentDish = DishCollection.getDishById(Integer.parseInt(choosenDish));

        if(currentDish != null) {
            tvDishName.setText(currentDish.getName());
            rbRating.setRating(currentDish.getRating());
            tvOther.setText(currentDish.getOther());
            tvBiggerDescription.setText(currentDish.getDescription() + " " + currentDish.getDetailedDescription());
            try {
                // ivPicture.setImageResource(R.drawable.mainpicture);
                new DownloadImageTask(ivPicture)
                        .execute(currentDish.getImage());
            }
            catch (Exception e)
            {
                Log.d("ERROR1", "message: " + e.getMessage() );
                ivPicture.setImageResource(R.drawable.nopicture);
            }
        }
        try {
            rbRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
        @Override
        public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

            Log.d("Action", "ratingBar clicked" );
            currentDish.setIsNumberOfRatingSet(false);
            currentDish.rate(rating);
            rbRating.setRating(currentDish.getRating());
            rbRating.setIsIndicator(true);
        }
    });
        }
        catch (Exception e)
        {
            tvDishName.setText(e.getMessage());
        }

        lvComments = findViewById(R.id.lvComments);

        ArrayList<Comment> coms = DishCollection.getComments();
        //coms.add(new Comment(1, "Sofia Krutko", "Very tasty"));
        //coms.add(new Comment(1, "Serhei Krutko", "I think it is very good!"));

        commentsForCurrentCountry = sortComments(currentDish.getDishId(), coms);
        tvNumberOfComments.setText(commentsForCurrentCountry.size() + " " + tvNumberOfComments.getText());

        commentAdapter = new CommentAdapter(
                getApplicationContext(),
                R.layout.comment_layout,
                commentsForCurrentCountry
        );

        lvComments.setAdapter(commentAdapter);

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }

    public class CommentAdapter extends ArrayAdapter {

        private List<Comment> comments;
        private int resource;
        private LayoutInflater inflater;

    public CommentAdapter(Context context, int resource, List<Comment> objects) {
        super(context, resource, objects);
        comments = objects;
        this.resource = resource;
        inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        try {
            Comment currentComment = comments.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(resource, null);
            }

            TextView tvCommentatorName = convertView.findViewById(R.id.tvCommentatorName);
            TextView tvComment = convertView.findViewById(R.id.tvComment);


            tvCommentatorName.setText(currentComment.getCommentatorName());
            tvComment.setText(currentComment.getComment());

        }
        catch (Exception e)
        {
            Log.d("ERROR3", "message " + e.getMessage() );
        }

        return convertView;
    }




}

private ArrayList<Comment> sortComments(int dishId, ArrayList<Comment> comments)
{
    ArrayList<Comment> result = new ArrayList<>();
    for (Comment c:comments
         ) {
        if(c.getDishId() == dishId)
            result.add(c);
    }
    return result;
}
}