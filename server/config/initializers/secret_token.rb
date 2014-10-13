# Be sure to restart your server when you modify this file.

# Your secret key is used for verifying the integrity of signed cookies.
# If you change this key, all old signed cookies will become invalid!

# Make sure the secret is at least 30 characters and all random,
# no regular words or you'll be exposed to dictionary attacks.
# You can use `rake secret` to generate a secure secret key.

# Make sure your secret_key_base is kept private
# if you're sharing your code publicly.
Network::Application.config.secret_key_base = '1fb9a2a3f1e27cf22c3b8406592519311645f28f0f985308895bf78e37cd7e1e905017595bc767edc61c15796d020c4e16cf95d9a43796e6c109e1126fdf7478'
