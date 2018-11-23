package services

import com.google.inject.{Inject, Singleton}
import daos.ChatDao

@Singleton
class ChatService @Inject()(chatDao: ChatDao) {

  def all = chatDao.all

  def findByIds(ids: List[Int]) = chatDao.findByIds(ids)

  def findById(id: Int) = chatDao.findById(id)

  def findGroupChats(userId: Int) = chatDao.findGroupChats(userId)

  def findUserChat(first: Int, second: Int) = chatDao.findUserChat(first, second)

  def findUserChat(userId: Int) = chatDao.findUserChat(userId)

  def createUserChat(first: Int, second: Int, groupId: Int) = chatDao.createUserChat(first, second, groupId)

  def createGroupChat(userIds: List[Int], groupId: Int) = chatDao.createGroupChat(userIds, groupId)

  def findGroupChat(id: Int) = chatDao.findGroupChat(id)
}
