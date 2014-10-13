class CreateConversationManagers < ActiveRecord::Migration
  def change
    create_table :conversation_managers do |t|
      t.references :user, index: true
      t.references :conversation, index: true

      t.timestamps
    end
  end
end
