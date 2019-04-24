package services

import com.google.inject.{Inject, Singleton}
import daos.MessageDao
import models.{Chat, Message}
import org.bson.types.ObjectId
import utils.Helper.StringExtended

@Singleton
class MessageService @Inject()(
  messageDao: MessageDao,
  socketNotificationService: SocketNotificationService
)(implicit configService: ConfigService) {

  implicit class MessageExtended(m: Message) {
    def encode = m.copy(text = m.text.encodeToken)

    def decode = {
      for {
        json <- m.text.decodeToken
        text <- (json \ "text").asOpt[String]
      } yield m.copy(text = text)
    }
  }

  def all = messageDao.all.flatMap(_.decode)

  def save(message: Message)(chat: Chat) = {
    val result = messageDao.dao.save(message.encode)
    if (result.wasAcknowledged()) {
      socketNotificationService.newMessage(message, chat)
    }
    result
  }

  def findByChatId(chatId: Int, page: Int) = messageDao.findByChatId(chatId, page: Int).flatMap(_.decode)

  def findById(id: ObjectId) = messageDao.findById(id).flatMap(_.decode)

  def read(id: ObjectId)(chat: Chat) = {
    val result = messageDao.markRead(id)
    result match {
      case Some(message) => socketNotificationService.markRead(id, chat, message)
      case _ =>
    }
    result
  }

  def delivery(id: ObjectId)(chat: Chat) = {
    val result = messageDao.markDelivery(id)
    result match {
      case Some(message) => socketNotificationService.markDelivery(id, chat, message)
      case _ =>
    }
    result
  }

  def findUnreadMessages(id: Int, userId: Int) = messageDao.findUnreadMessages(id, userId).flatMap(_.decode)

  def findUnreadMessagesCount(id: Int, from: Int) = messageDao.findUnreadMessagesCount(id, from)

  def findUnreadMessagesCount(id: Int) = messageDao.findUnreadMessagesCount(id)

  def findLastMessage(id: Int) = messageDao.findLastMessage(id).flatMap(_.decode)

  def change(id: ObjectId, userId: Int, key: String, text: String)(groupId: Int, chat: Chat) = {
    val result = messageDao.change(id, userId, key, text.encodeToken)
    if (result.isUpdateOfExisting) socketNotificationService.changed(groupId, id, chat.userIds)
    result
  }

  def softDelete(id: ObjectId)(groupId: Int, chat: Chat) = {
    val result = messageDao.softDelete(id)
    if (result.isUpdateOfExisting) socketNotificationService.softDelete(groupId, id, chat.userIds)
    result
  }

  def remove(id: ObjectId)(groupId: Int, chat: Chat) = {
    val result = messageDao.removeById(id)
    if (result.isUpdateOfExisting) socketNotificationService.remove(groupId, id, chat.userIds)
    result
  }

  def findChatIdByObjectId(id: ObjectId) = messageDao.findChatIdByObjectId(id)

  def findMessagesAfter(chatId: Int, messageId: ObjectId) = messageDao.findMessagesAfter(chatId, messageId)
}
