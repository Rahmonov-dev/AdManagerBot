package org.example.handler;

import org.example.keyboardUtils.LanguageKeyboards;
import org.example.keyboardUtils.SubscriptionKeyboards;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.MessageService;
import org.example.service.UserService;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.example.model.Advertisement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.keyboardUtils.AdminUserKeyboard.createAdminKeyboard;
import static org.example.keyboardUtils.AdminUserKeyboard.createAdminUserKeyboard;
import static org.example.keyboardUtils.LanguageKeyboards.languageKeyboard;
import static org.example.keyboardUtils.UserKeyboard.createUserKeyboard;

import org.example.utils.ResourceBundleManager;


public class MessageHandler {
    // Temporary storage for advertisement data
    private static final Map<Long, Advertisement> tempAds = new ConcurrentHashMap<>();


    public static BotApiMethod<?> handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        User user = UserService.getByChatId(message);
        Status status = user.getStatus();
        if ("/start".equals(text) && user.isAdmin()) {
            user.setStatus(Status.ADMIN_PANEL);
            UserService.update(user);
            return MessageService.createMessage(chatId, "Admin panelga xush kelibsiz!", createAdminUserKeyboard(user));
        } else if ("/start".equals(text)) {
            user.setStatus(Status.START);
            UserService.update(user);
            return handleStart(message, user);
        }
        switch (status) {
            case START:
                return handleStart(message, user);
            case WAITING_FOR_LANGUAGE_SELECTION:
                return handleLanguageSelection(message, user);
            case ADMIN_PANEL:
                return handleAdminPanel(message, user);
            case USER_PANEL:
                return handleUserPanel(message, user);
                
            // Advertisement flow states - handle text inputs
            case WAITING_FOR_AD_TEXT:
            case WAITING_FOR_AD_PRICE:
            case WAITING_FOR_AD_LOCATION:
            case WAITING_FOR_AD_DESCRIPTION:
            case WAITING_FOR_AD_CONTACT:
            case WAITING_FOR_AD_CONFIRMATION:
                return handleAdText(message, user);
                
            // Handle photo uploads
            case WAITING_FOR_AD_PHOTO:
                System.out.println("DEBUG: In WAITING_FOR_AD_PHOTO. hasPhoto: " + message.hasPhoto() + 
                                 ", hasText: " + message.hasText() + 
                                 ", message: " + message);
                if (message.hasPhoto()) {
                    System.out.println("DEBUG: Routing to handleMedia");
                    return handleMedia(message, user);
                } else {
                    System.out.println("DEBUG: No photo, routing to handleAdText");
                    return handleAdText(message, user);
                }
                
            case WAITING_FOR_MEDIA:
                return handleMedia(message, user);
            case WAITING_FOR_CONFIRMATION:
                return handleConfirmation(message, user);
            case WAITING_FOR_SCHEDULE_TIME:
                return handleScheduleTime(message, user);
            case WAITING_FOR_PAYMENT:
                return handlePayment(message, user);
            case SENDING_AD:
                return handleSendingAd(message, user);
            case FINISHED:
                return handleFinished(message, user);
            case CANCELLED:
                return handleCancelled(message, user);
            case UNKNOWN:
            default:
                return MessageService.createMessage(chatId, "Nomaʼlum holat. /start buyrugʻini yuboring.");
        }
    }

    public static BotApiMethod<?> handleUserPanel(Message message, User user) {
        String text = message.getText();
        
        // Check if user clicked on "Reklama berish" button
        if (text != null && text.equals(ResourceBundleManager.getMessage("button.send_ad", user.getLanguage()))) {
            // Create new advertisement
            Advertisement ad = new Advertisement();
            ad.setUserId(message.getChatId());
            ad.setCreatedAt(LocalDateTime.now());
            tempAds.put(message.getChatId(), ad);
            
            // Ask for product name
            user.setStatus(Status.WAITING_FOR_AD_TEXT);
            UserService.update(user);
            return MessageService.createMessage(message.getChatId(), 
                ResourceBundleManager.getMessage("ad.ask_product_name", user.getLanguage()));
        }
        
        // Default behavior - show user panel
        user.setStatus(Status.USER_PANEL);
        UserService.update(user);
        return MessageService.createMessage(
                message.getChatId(),
                ResourceBundleManager.getMessage("user_panel", user.getLanguage()),
                createUserKeyboard(user)
        );
    }

    public static BotApiMethod<?> handleAdminPanel(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();
        
        if (text != null && text.contains(ResourceBundleManager.getMessage("button.user_panel",user.getLanguage()))) {
            return handleUserPanel(message, user);
        } else if (text != null && text.contains(ResourceBundleManager.getMessage("button.admin_panel",user.getLanguage()))) {
            return MessageService.createMessage(
                chatId,
                ResourceBundleManager.getMessage("admin_panel", user.getLanguage()),
                createAdminKeyboard(user)
            );
        }

        if (user.isAdmin()) {
            return MessageService.createMessage(
                chatId, 
                ResourceBundleManager.getMessage("admin_panel", user.getLanguage()),
                createAdminUserKeyboard(user)
            );
        } else {
            return handleUserPanel(message, user);
        }
    }

    private static BotApiMethod<?> handleCancelled(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleFinished(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handlePayment(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleSendingAd(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleScheduleTime(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleConfirmation(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();
        Advertisement ad = tempAds.get(chatId);
        
        if (ad == null) {
            return handleUserPanel(message, user);
        }
        
        if (text != null) {
            if (text.startsWith("✅")) {
                // Save the advertisement (in a real app, save to database)
                // For now, just send it back
                String adText = ad.toFormattedString();
                // Clear temporary data
                tempAds.remove(chatId);
                user.setStatus(Status.USER_PANEL);
                UserService.update(user);
                
                // Send the formatted ad
                return MessageService.createMessage(chatId, adText, createUserKeyboard(user));
                
            } else if (text.startsWith("❌")) {
                // Cancel the ad
                tempAds.remove(chatId);
                user.setStatus(Status.USER_PANEL);
                UserService.update(user);
                return MessageService.createMessage(chatId, 
                    ResourceBundleManager.getMessage("ad.cancelled", user.getLanguage()),
                    createUserKeyboard(user));
            }
        }
        
        return MessageService.createMessage(chatId, 
            ResourceBundleManager.getMessage("error.invalid_choice", user.getLanguage()));
    }

    private static BotApiMethod<?> handleMedia(Message message, User user) {
        Long chatId = message.getChatId();
        System.out.println("DEBUG: handleMedia called. Status: " + user.getStatus() + 
                         ", hasPhoto: " + message.hasPhoto() + 
                         ", chatId: " + chatId);
        
        // Handle photo upload
        if (user.getStatus() == Status.WAITING_FOR_AD_PHOTO) {
            System.out.println("DEBUG: User is in WAITING_FOR_AD_PHOTO state");
            
            if (message.hasPhoto()) {
                System.out.println("DEBUG: Message contains photos");
                Advertisement ad = tempAds.get(chatId);
                if (ad != null) {
                    try {
                        // Get the largest available photo (last in the array is the highest resolution)
                        List<PhotoSize> photos = message.getPhoto();
                        System.out.println("DEBUG: Found " + photos.size() + " photo sizes");
                        
                        PhotoSize photo = photos.get(photos.size() - 1);
                        String fileId = photo.getFileId();
                        System.out.println("DEBUG: Selected photo - FileID: " + fileId + 
                                         ", width: " + photo.getWidth() + 
                                         ", height: " + photo.getHeight());
                        
                        ad.setPhotoFileId(fileId);
                        tempAds.put(chatId, ad); // Update the ad in the map
                        
                        // Move to next step
                        user.setStatus(Status.WAITING_FOR_AD_CONTACT);
                        UserService.update(user);
                        
                        System.out.println("DEBUG: Photo processed successfully, moving to contact info");
                        return MessageService.createMessage(chatId, 
                            ResourceBundleManager.getMessage("ad.ask_contact", user.getLanguage()));
                            
                    } catch (Exception e) {
                        System.err.println("ERROR processing photo: " + e.getMessage());
                        e.printStackTrace();
                        return MessageService.createMessage(chatId, 
                            "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring. " + e.getMessage());
                    }
                } else {
                    System.err.println("ERROR: No advertisement found for chat " + chatId);
                    return MessageService.createMessage(chatId, 
                        "Xatolik yuz berdi. Iltimos, qaytadan boshlang /start");
                }
            } else {
                System.err.println("ERROR: No photo found in the message");
                return MessageService.createMessage(chatId, 
                    "Iltimos, faqat rasm yuboring.");
            }
        } else {
            System.err.println("ERROR: Unexpected status: " + user.getStatus() + " (expected WAITING_FOR_AD_PHOTO)");
            return MessageService.createMessage(chatId, 
                "Iltimos, avval mahsulot haqida ma'lumotlarni kiriting.");
        }
    }

    private static BotApiMethod<?> handleAdText(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();
        
        // Check if user clicked on "Reklama berish" button
        if (text != null && text.equals(ResourceBundleManager.getMessage("button.send_ad", user.getLanguage()))) {
            return startNewAdvertisement(chatId, user);
        }
        
        switch (user.getStatus()) {
            case WAITING_FOR_AD_TEXT:
                return handleProductNameInput(chatId, text, user);
            case WAITING_FOR_AD_PRICE:
                return handlePriceInput(chatId, text, user);
            case WAITING_FOR_AD_LOCATION:
                return handleLocationInput(chatId, text, user);
            case WAITING_FOR_AD_DESCRIPTION:
                return handleDescriptionInput(chatId, text, user);
            case WAITING_FOR_AD_CONTACT:
                return handleContactInput(chatId, text, user);
            default:
                return getErrorMessage(chatId, user);
        }
    }
    
    private static BotApiMethod<?> startNewAdvertisement(Long chatId, User user) {
        Advertisement ad = new Advertisement();
        ad.setUserId(chatId);
        ad.setCreatedAt(LocalDateTime.now());
        tempAds.put(chatId, ad);
        
        user.setStatus(Status.WAITING_FOR_AD_TEXT);
        UserService.update(user);
        return MessageService.createMessage(chatId, 
            ResourceBundleManager.getMessage("ad.ask_product_name", user.getLanguage()));
    }
    
    private static BotApiMethod<?> handleProductNameInput(Long chatId, String productName, User user) {
        Advertisement ad = tempAds.get(chatId);
        if (ad != null) {
            ad.setProductName(productName);
            user.setStatus(Status.WAITING_FOR_AD_PRICE);
            UserService.update(user);
            return MessageService.createMessage(chatId, 
                ResourceBundleManager.getMessage("ad.ask_price", user.getLanguage()));
        }
        return getErrorMessage(chatId, user);
    }
    
    private static BotApiMethod<?> handlePriceInput(Long chatId, String price, User user) {
        Advertisement ad = tempAds.get(chatId);
        if (ad != null) {
            ad.setPrice(price);
            user.setStatus(Status.WAITING_FOR_AD_LOCATION);
            UserService.update(user);
            return MessageService.createMessage(chatId, 
                ResourceBundleManager.getMessage("ad.ask_location", user.getLanguage()));
        }
        return getErrorMessage(chatId, user);
    }
    
    private static BotApiMethod<?> handleLocationInput(Long chatId, String location, User user) {
        Advertisement ad = tempAds.get(chatId);
        if (ad != null) {
            ad.setLocation(location);
            user.setStatus(Status.WAITING_FOR_AD_DESCRIPTION);
            UserService.update(user);
            return MessageService.createMessage(chatId, 
                ResourceBundleManager.getMessage("ad.ask_description", user.getLanguage()));
        }
        return getErrorMessage(chatId, user);
    }
    
    private static BotApiMethod<?> handleDescriptionInput(Long chatId, String description, User user) {
        Advertisement ad = tempAds.get(chatId);
        if (ad != null) {
            ad.setDescription(description);
            user.setStatus(Status.WAITING_FOR_AD_PHOTO);
            UserService.update(user);
            return MessageService.createMessage(chatId, 
                ResourceBundleManager.getMessage("ad.ask_photo", user.getLanguage()));
        }
        return getErrorMessage(chatId, user);
    }
    
    private static BotApiMethod<?> handleContactInput(Long chatId, String contactInfo, User user) {
        Advertisement ad = tempAds.get(chatId);
        if (ad != null) {
            ad.setContactInfo(contactInfo);
            user.setStatus(Status.WAITING_FOR_AD_CONFIRMATION);
            UserService.update(user);
            
            String confirmationMessage = String.format(
                ResourceBundleManager.getMessage("ad.confirm", user.getLanguage()),
                ad.getProductName(),
                ad.getPrice(),
                ad.getLocation(),
                ad.getDescription(),
                ad.getContactInfo()
            );
            
            return MessageService.createMessage(chatId, confirmationMessage, createConfirmationKeyboard(user));
        }
        return getErrorMessage(chatId, user);
    }
    
    private static ReplyKeyboardMarkup createConfirmationKeyboard(User user) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add("✅ " + ResourceBundleManager.getMessage("button.confirm", user.getLanguage()));
        row.add("❌ " + ResourceBundleManager.getMessage("button.cancel", user.getLanguage()));
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
    
    private static BotApiMethod<?> getErrorMessage(Long chatId, User user) {
        return MessageService.createMessage(chatId, 
            ResourceBundleManager.getMessage("error.unknown_command", user.getLanguage()));
    }


    private static BotApiMethod<?> handleLanguageSelection(Message message, User user) {
        String text = message.getText();
        Long chatId = message.getChatId();
        SendMessage sendMessage;
        String messageText;
        boolean languageSelected = processLanguageSelection(text, user);

        if (!languageSelected) {
            messageText = "Iltimos, quyidagi tillardan birini tanlang:\nПожалуйста, выберите один из языков:";
            sendMessage = MessageService.createMessage(chatId, messageText, LanguageKeyboards.languageKeyboard());
        } else {
            messageText = ResourceBundleManager.getMessage("ask_for_join", user.getLanguage());
            sendMessage = MessageService.
                    createMessage(chatId, messageText, SubscriptionKeyboards.forceSubscribe(user));
        }

        return sendMessage;
    }

    private static boolean processLanguageSelection(String text, User user) {
        if (text == null) {
            return false;
        }

        String language = null;
        if (text.contains("O'zbekcha")) {
            language = "uz";
        } else if (text.contains("Русский")) {
            language = "ru";
        } else {
            return false;
        }

        user.setLanguage(language);
        user.setStatus(Status.WAITING_FOR_CHANNEL_VERIFICATION);
        UserService.update(user);
        return true;
    }


    private static BotApiMethod<?> handleStart(Message message, User user) {
        Long chatId = message.getChatId();
        user.setStatus(Status.WAITING_FOR_LANGUAGE_SELECTION);
        UserService.update(user);

        String messageText = ResourceBundleManager.getMessage("select_language", user.getLanguage());
        SendMessage sendMessage = MessageService.createMessage(chatId, messageText, languageKeyboard());

        return sendMessage;
    }


}
