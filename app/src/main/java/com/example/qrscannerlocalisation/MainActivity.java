package com.example.qrscannerlocalisation;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Arrays;


public class MainActivity extends AppCompatActivity {

    // Création des variables
    boolean getPositionQR = false;
    double[] positionsQRCode = {0, 0};

    Button btn_scan;

    /**
     * Création de l'activité
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Récupération des données
        btn_scan = findViewById(R.id.btn_scan);

        // Quand on click sur le bouton cela exécute la méthode : scanCode()
        btn_scan.setOnClickListener(view -> scanCode());
    }

    /**
     * Méthode appelée lorsque l'on clique sur le bouton "Scan Me"
     */
    private void scanCode() {
        // S'occupe de scanner notre code QR
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan un QR code");
        options.setBeepEnabled(false);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        barLauncher.launch(options);
    }

    /**
     * Permet le lancement de l'activité map
     */
    public void releaseMapActivity() {
        // Si une position a été récupérée depuis un QR code
        Intent intent = new Intent(MainActivity.this, Localisation.class);
        // Ajouter par défaut si une position a été récupérée depuis le QR code.
        intent.putExtra("getPositionQR", getPositionQR);
        // Si une position a été récupérée depuis un QR code, ajouter les coordonnées
        if (getPositionQR) {
            // Lancement de l'activité de la carte avec la position récupérée
            intent.putExtra("latitudeQR", positionsQRCode[0]);
            intent.putExtra("longitudeQR", positionsQRCode[1]);
        }
        // lancement de l'activité
        startActivity(intent);
    }

    /**
     * Récupération du résultat du scan
     */
    ActivityResultLauncher<ScanOptions> barLauncher = registerForActivityResult(new ScanContract(), result -> {
        // Si le resultat contient quelque chose
        if (result.getContents() != null) {
            // Création d'une boite de dialogue
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            // Ajout de paramètres
            // Titre
            builder.setTitle("Scan Result");
            // Message
            builder.setMessage(result.getContents());
            // Traiter le résultat du scan
            processResult(result.getContents());
            // Bouton de validation/fermeture
            builder.setPositiveButton("OK", (dialogInterface, i) -> {
                // Ferme la boite de dialogue
                dialogInterface.dismiss();
                // Si une position a été récupérée depuis un QR code, lancer l'activité de carte
                if (getPositionQR) {
                    releaseMapActivity();
                }
            }).show();
        }
    });

    /**
     * Permet de traiter le résultat du scan
     *
     * @param content le contenu de QR code
     */
    private void processResult(String content) {
        // Met à zero les valeurs
        getPositionQR = false;
        Arrays.fill(positionsQRCode, 0);
        // Vérifie si le contenu du QR code est bien un QR code de géolocalisation
        if (content.contains("geo:") || content.contains("GEO:")) {
            // Garde uniquement les chiffres, points et virgules
            content = content.replaceAll("[^0-9.,]", "");
            // Récupére les 2 positions
            String[] positions = content.split(",");
            // Convertis les positions en double
            positionsQRCode[0] = Double.parseDouble(positions[0]);
            positionsQRCode[1] = Double.parseDouble(positions[1]);
            // Met à jours les variables pour le changement d'activité
            addDataForIntentupdateDataForIntent(true, positionsQRCode);
        } else {
            addDataForIntentupdateDataForIntent(false, positionsQRCode);
        }
    }

    /**
     * Met à jour les variables pour le changement de l'activité
     *
     * @param getPositionFromQR si le QR code est un QR code de géolocalisation
     * @param positionsFromQR   Les positions du QR code
     */
    public void addDataForIntentupdateDataForIntent(boolean getPositionFromQR, double[] positionsFromQR) {
        this.getPositionQR = getPositionFromQR;
        this.positionsQRCode = positionsFromQR;
    }
}