package com.example.qrscannerlocalisation;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Localisation extends AppCompatActivity implements OnMapReadyCallback {

    // Si il doit recevoir une position venant d'un qrcode
    boolean getPositionQR = false;
    // Position venant du QR code
    double latitudeQR = 0;
    double longitudeQR = 0;

    private GoogleMap mMap;

    // Instanciation des éléments venant du fragment message
    FragmentMessage fragmentMessage;
    Button bt_message_envoyer;
    EditText et_message;
    EditText et_phone;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_localisation);

        // Récupération du fragment
        fragmentMessage = (FragmentMessage) getSupportFragmentManager().findFragmentById(R.id.message);

        // Récupérer les éléments duu fragment
        bt_message_envoyer = fragmentMessage.getView().findViewById(R.id.bt_message_envoyer);
        et_message = fragmentMessage.getView().findViewById(R.id.et_message);
        et_phone = fragmentMessage.getView().findViewById(R.id.et_phone);

        // Récupération des données de l'activité précédente (MainActivity)
        Intent intent = getIntent();

        // Si une position est envoyée
        getPositionQR = intent.getBooleanExtra("getPositionQR", false);
        latitudeQR = intent.getDoubleExtra("latitudeQR", 0);
        longitudeQR = intent.getDoubleExtra("longitudeQR", 0);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(Localisation.this);

        // Création du message à envoyer avec la position
        et_message.setText(getCoordonateMessage());

        // Action à éffectuer lors du clic sur le bouton d'envoi de message
        bt_message_envoyer.setOnClickListener(v -> {
            System.out.println("Envois du message...");

            // Demande de la permission d'envoyer un SMS
            if (ContextCompat.checkSelfPermission(Localisation.this, Manifest.permission.SEND_SMS)
                    == PackageManager.PERMISSION_GRANTED) {

                // Si autorisation est accordée
                sendMessage();

            } else {

                // Si autorisation n'est pas accordée
                ActivityCompat.requestPermissions(Localisation.this, new String[]
                        {Manifest.permission.SEND_SMS}, 200);
            }
        });
    }

    /**
     * Création du message à envoyer avec la position
     *
     * @return message à envoyer
     */
    private String getCoordonateMessage() {
        System.out.println("getCoordonateMessage");
        String message = "Ma position : \n";
        message += "https://www.google.com/maps/search/?api=1&query=" + latitudeQR + "," + longitudeQR + "\n";
        message += "Bonne journée";
        return message;
    }

    /**
     * Création de la méthode pour envoyer le SMS
     */
    private void sendMessage() {
        System.out.println("sendMessage");

        // Récupération des valeurs
        String message = et_message.getText().toString();
        String phoneNum = et_phone.getText().toString();

        // Vérificaiton du contenu
        if (message.isEmpty() || phoneNum.isEmpty()) {

            Toast.makeText(Localisation.this, "Veuillez remplir tous les champs",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Initialiser le gestionnaire du SMS
            SmsManager smsManager = SmsManager.getDefault();

            // Envois du message
            smsManager.sendTextMessage(phoneNum, null, message, null, null);

            // Pop up de confirmation
            Toast.makeText(getApplicationContext(), "Message bien envoyé", Toast.LENGTH_LONG).show();
            System.out.println("Message envoyé");
        }
    }

    /**
     * Méthode appelée lorsque l'utilisateur a les autorisations nécessaires
     *
     * @param requestCode  Code de la demande d'autorisation
     * @param permissions  Tableau des permissions demandées
     * @param grantResults Tableau des réponses de l'utilisateur
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        System.out.println("onRequestPermissionsResult");
        // Vérification des conditions
        if (requestCode == 200 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Si l'autorisation est accordée...
            sendMessage();
        } else {

            // Si l'autorisation n'est pas accordée...
            Toast.makeText(Localisation.this, "Permission refusée", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        System.out.println("onMapReady");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            createMarker(latitudeQR, longitudeQR, "Voici le lieux", googleMap);
        }
    }

    /**
     * Création d'un marqueur sur la carte
     *
     * @param latitude  Latitude du marqueur
     * @param longitude Longitude du marqueur
     * @param title     Titre du marqueur
     * @param googleMap Carte sur laquelle on ajoute le marqueur
     */
    private void createMarker(double latitude, double longitude, String title, GoogleMap googleMap) {
        System.out.println("Création du marqueur...");

        // Enlever sur la map les marqueurs précédents
        googleMap.clear();

        // Créer un marqueur sur la carte avec les coordonnées
        LatLng userPosition = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions()
                // Position du markeur
                .position(userPosition)
                // Titre du marqueur
                .title(title));

        // Zoom à la position du marqueur
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userPosition, 14));
        System.out.println("Marqueur créé !");
    }
}