package com.transur.sigem.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Value("${cloudinary.upload-folder:sigem}")
    private String uploadFolder;

    /**
     * Sube una imagen a Cloudinary y devuelve su URL HTTPS.
     * Cloudinary es opcional: si no está configurado, se informa claramente
     * para que el usuario pueda seguir usando URLs de imagen desde el catálogo.
     */
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Debes seleccionar una imagen.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Solo se permiten archivos de imagen.");
        }
        ensureConfigured();

        try {
            Cloudinary cloudinary = client();

            Map<?, ?> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap("folder", uploadFolder)
            );

            Object secureUrl = result.get("secure_url");
            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new IllegalStateException("Cloudinary no devolvió una URL segura para la imagen.");
            }
            return secureUrl.toString();
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la imagen seleccionada.", e);
        } catch (Exception e) {
            if (e instanceof IllegalStateException) throw (IllegalStateException) e;
            throw new IllegalStateException("No se pudo subir la imagen a Cloudinary: " + e.getMessage(), e);
        }
    }
    /** Importa una imagen pública desde una URL HTTP/HTTPS a Cloudinary. */
    public String uploadImageFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar una URL de imagen.");
        }
        String url = imageUrl.trim();
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new IllegalArgumentException("La URL de imagen debe comenzar con http:// o https://.");
        }
        ensureConfigured();
        try {
            Map<?, ?> result = client().uploader().upload(url, ObjectUtils.asMap("folder", uploadFolder));
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new IllegalStateException("Cloudinary no devolvió una URL segura para la imagen remota.");
            }
            return secureUrl.toString();
        } catch (Exception e) {
            if (e instanceof IllegalArgumentException || e instanceof IllegalStateException) throw (RuntimeException) e;
            throw new IllegalStateException("No se pudo importar la imagen desde la URL: " + e.getMessage(), e);
        }
    }

    /** Si Cloudinary está configurado importa la URL; si no, conserva la URL directa. */
    public String guardarUrlOImportar(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Debes ingresar una URL de imagen.");
        }
        String url = imageUrl.trim();
        if (!(url.startsWith("https://") || url.startsWith("http://"))) {
            throw new IllegalArgumentException("La URL de imagen debe comenzar con http:// o https://.");
        }
        return estaConfigurado() ? uploadImageFromUrl(url) : url;
    }

    public boolean estaConfigurado() {
        return cloudName != null && !cloudName.isBlank()
                && apiKey != null && !apiKey.isBlank()
                && apiSecret != null && !apiSecret.isBlank();
    }

    private void ensureConfigured() {
        if (!estaConfigurado()) {
            throw new IllegalStateException(
                    "La carga de archivos no está configurada. Completa CLOUDINARY_CLOUD_NAME, "
                            + "CLOUDINARY_API_KEY y CLOUDINARY_API_SECRET.");
        }
    }

    private Cloudinary client() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

}
