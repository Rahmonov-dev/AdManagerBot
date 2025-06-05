package org.example.handler;

import org.example.model.Advertisement;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.MessageService;
import org.example.service.UserService;
import org.example.utils.ResourceBundleManager;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles media files (photos, documents, etc.) sent by users
 */

public class MediaHandler {
    private static final Map<Long, Advertisement> tempAds = new ConcurrentHashMap<>();
    
    /**
     * Handles media files sent by users during advertisement creation
     * @param message The message containing the media
     * @param user The user who sent the message
     * @return Response message to send back to the user
     */
    public static Object handleMedia(Message message, User user) {
        try {
            Long chatId = message.getChatId();
            System.out.println("DEBUG: handleMedia called. Status: " + user.getStatus() +
                    ", hasPhoto: " + message.hasPhoto() +
                    ", chatId: " + chatId);

            // Handle photo upload
            if (user.getStatus() == Status.WAITING_FOR_AD_PHOTO || 
                user.getStatus() == Status.WAITING_FOR_AD_CONFIRMATION) {
                
                if (message.hasPhoto()) {
                    return handlePhotoUpload(message, user);
                } else {
                    System.out.println("DEBUG: No photo in message, asking user to send a photo");
                    return MessageService.createMessage(chatId,
                            ResourceBundleManager.getMessage("ad.ask_photo_again", user.getLanguage()));
                }
            }
            
            // If we get here, the media was sent in an unexpected state
            System.out.println("WARN: handleMedia called in wrong status: " + user.getStatus());
            return MessageService.createMessage(chatId,
                    ResourceBundleManager.getMessage("error.unexpected_error", user.getLanguage()));
                    
        } catch (Exception e) {
            System.err.println("Error in handleMedia: " + e.getMessage());
            e.printStackTrace();
            return MessageService.createMessage(message.getChatId(),
                    ResourceBundleManager.getMessage("error.unexpected_error", "en") + 
                    "\n" + e.getMessage());
        }
    }
    
    /**
     * Handles photo upload during advertisement creation
     */
    private static Object handlePhotoUpload(Message message, User user) {
        Long chatId = message.getChatId();
        List<PhotoSize> photos = message.getPhoto();
        PhotoSize photo = photos.get(photos.size() - 1);  // Get the largest photo
        String fileId = photo.getFileId();
        
        System.out.println("DEBUG: Received photo with fileId: " + fileId);

        // Get ad from temp storage
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            System.err.println("ERROR: No ad found in tempAds for chatId: " + chatId);
            return MessageService.createMessage(chatId,
                    ResourceBundleManager.getMessage("error.ad_not_found", user.getLanguage()));
        }
        
        // Update ad with photo
        System.out.println("DEBUG: Found ad in tempAds, setting photoFileId");
        ad.setPhotoFileId(fileId);
        saveTempAd(chatId, ad);
        System.out.println("DEBUG: Updated ad in tempAds: " + ad);

        // Move to next step
        user.setStatus(Status.WAITING_FOR_AD_CONTACT);
        UserService.update(user);
        System.out.println("DEBUG: User status updated to WAITING_FOR_AD_CONTACT");

        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("ad.ask_contact", user.getLanguage()));
    }
    
    /**
     * Saves an advertisement to temporary storage
     */
    public static void saveTempAd(Long chatId, Advertisement ad) {
        if (chatId != null && ad != null) {
            tempAds.put(chatId, ad);
        }
    }
    
    /**
     * Retrieves an advertisement from temporary storage
     */
    public static Advertisement getTempAd(Long chatId) {
        return chatId != null ? tempAds.get(chatId) : null;
    }
    
    /**
     * Removes an advertisement from temporary storage
     */
    public static void removeTempAd(Long chatId) {
        if (chatId != null) {
            tempAds.remove(chatId);
        }
    }
}
